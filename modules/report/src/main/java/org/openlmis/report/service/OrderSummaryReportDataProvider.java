/*
 * Electronic Logistics Management Information System (eLMIS) is a supply chain management system for health commodities in a developing country setting.
 *
 * Copyright (C) 2015  John Snow, Inc (JSI). This program was produced for the U.S. Agency for International Development (USAID). It was prepared under the USAID | DELIVER PROJECT, Task Order 4.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openlmis.report.service;

import lombok.NoArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.openlmis.core.domain.*;
import org.openlmis.core.service.*;
import org.openlmis.order.service.OrderService;
import org.openlmis.report.mapper.OrderSummaryReportMapper;
import org.openlmis.report.model.ResultRow;
import org.openlmis.report.model.dto.Schedule;
import org.openlmis.report.model.params.OrderReportParam;
import org.openlmis.report.service.lookup.ReportLookupService;
import org.openlmis.report.util.ParameterAdaptor;
import org.openlmis.report.util.StringHelper;
import org.openlmis.rnr.domain.RequisitionStatusChange;
import org.openlmis.rnr.domain.Rnr;
import org.openlmis.rnr.domain.RnrStatus;
import org.openlmis.rnr.service.RequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.lang.model.element.NestingKind;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor
public class OrderSummaryReportDataProvider extends ReportDataProvider {

    public static final String REGION = "REGION";
    public static final String DISTRICT = "DISTRICT";
    public static final String FAX = "FAX";
    public static final String PHONE = "PHONE";
    public static final String FACILITY_ADDRESS = "FACILITY_ADDRESS";
    public static final String APPROVED_DATE = "APPROVED_DATE";
    public static final String APPROVED_BY = "APPROVED_BY";
    public static final String IN_APPROVAL_DATE = "IN_APPROVAL_DATE";
    public static final String IN_APPROVAL_BY = "IN_APPROVAL_BY";
    public static final String AUTHORIZED_DATE = "AUTHORIZED_DATE";
    public static final String AUTHORIZED_BY = "AUTHORIZED_BY";
    public static final String ORDER_NO = "ORDER_NO";
    public static final String ADDRESS = "ADDRESS";
    public static final String CUSTOM_REPORT_TITLE = "CUSTOM_REPORT_TITLE";

    public static final String PROGRAM = "PROGRAM";
    public static final String SCHEDULE = "SCHEDULE";
    public static final String YEAR = "YEAR";
    public static final String PERIOD = "PERIOD";
    public static final String DEPOT = "DEPOT";
    public static final String TYPE = "TYPE";

    @Autowired
    private OrderSummaryReportMapper reportMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ConfigurationSettingService configurationService;

    @Autowired
    private ReportLookupService reportLookupService;

    @Autowired
    private RequisitionService requistionService;

    @Autowired
    private ProcessingPeriodService periodService;

    @Autowired
    private ProcessingScheduleService scheduleSerive;

    @Autowired
    private FacilityService facilityService;


    @Autowired
    private ProgramService programService;

    @Override
    public List<? extends ResultRow> getReportBody(Map<String, String[]> filterCriteria, Map<String, String[]> sortCriteria, int page, int pageSize) {
        RowBounds rowBounds = new RowBounds((page - 1) * pageSize, pageSize);
        OrderReportParam orderReportParam = getReportFilterData(filterCriteria);
        orderReportParam.setTitle(getTitle(orderReportParam));

        return reportMapper.getOrderSummaryReport(orderReportParam, sortCriteria, rowBounds);
    }

    private String getTitle(OrderReportParam orderReportParam) {
        if(orderReportParam.getOrderId() == null || orderReportParam.getOrderId() == 0)
            return "";

        Rnr rnr = requistionService.getLWById(orderReportParam.getOrderId());
        Program program = programService.getById(rnr.getProgram().getId());
        ProcessingPeriod period = periodService.getById(rnr.getPeriod().getId());

        StringBuilder title = new StringBuilder("");
        title.append("Program: ")
                .append(program.getName())
                .append("\n").
                append("Period: ")
                .append(period.getName())
                .append(" - ")
                .append(period.getStringYear());
        return title.toString();
    }

    private OrderReportParam getReportFilterData(Map<String, String[]> filterCriteria) {
        OrderReportParam orderReportParam = ParameterAdaptor.parse(filterCriteria, OrderReportParam.class);
        if (orderReportParam.getOrderId() <= 0) {
            orderReportParam.setOrderId(reportMapper.getRequisitionId(orderReportParam.getFacility(), orderReportParam.getProgram(), orderReportParam.getPeriod()));
        }
        return orderReportParam;
    }

    @Override
    public String getFilterSummary(Map<String, String[]> params) {
        return getReportFilterData(params).toString();
    }

    @Override
    public Map<String, String> getExtendedHeader(Map filterCriteria) {
        OrderReportParam orderReportParam = getReportFilterData(filterCriteria);
        Map<String, String> result = new HashMap<>();
        result.put(ADDRESS, configurationService.getConfigurationStringValue(ConfigurationSettingKey.ORDER_REPORT_ADDRESS));
        result.put(CUSTOM_REPORT_TITLE, configurationService.getConfigurationStringValue(ConfigurationSettingKey.ORDER_REPORT_TITLE));
        result.put(ConfigurationSettingKey.ORDER_SUMMARY_SHOW_SIGNATURE_SPACE_FOR_CUSTOMER, configurationService.getConfigurationStringValue(ConfigurationSettingKey.ORDER_SUMMARY_SHOW_SIGNATURE_SPACE_FOR_CUSTOMER));
        result.put(ConfigurationSettingKey.ORDER_SUMMARY_SHOW_DISCREPANCY_SECTION, configurationService.getConfigurationStringValue(ConfigurationSettingKey.ORDER_SUMMARY_SHOW_DISCREPANCY_SECTION));


        Rnr rnr = requistionService.getLWById(orderReportParam.getOrderId());
        Program program = programService.getById(rnr.getProgram().getId());
        ProcessingPeriod period = this.periodService.getById(rnr.getPeriod().getId());
        ProcessingSchedule schedule = this.scheduleSerive.get(period.getScheduleId());
        Facility depotFacility = null;
        String supplyDepot = StringHelper.getValue(filterCriteria, "supplyDepot");
        if (supplyDepot != null)
            depotFacility = this.facilityService.getFacilityById(Long.parseLong(supplyDepot));
        String year = StringHelper.getValue(filterCriteria, "year");

        String type = rnr.isEmergency() ? "Emergency" : "Regular";

        String orderNo = orderService.getOrderNumberConfiguration().getOrderNumberFor(rnr.getId(), program, rnr.isEmergency());
        result.put(ORDER_NO, orderNo);
        // rnr.getSupplyingDepotId().


        result.put(PROGRAM, program.getName());
        if (year != null)
            result.put(YEAR, year);
        result.put(SCHEDULE, schedule.getName());
        result.put(PERIOD, period.getName());
        if (depotFacility != null)
            result.put(DEPOT, depotFacility.getName());
        result.put(TYPE, type);


        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");

        List<RequisitionStatusChange> changes = reportMapper.getLastUsersWhoActedOnRnr(orderReportParam.getOrderId(), RnrStatus.AUTHORIZED.name());
        if (!changes.isEmpty()) {
            result.put(AUTHORIZED_BY, changes.get(0).getCreatedBy().getFirstName() + " " + changes.get(0).getCreatedBy().getLastName());
            result.put(AUTHORIZED_DATE, dateFormat.format(changes.get(0).getCreatedDate()));
        }

        changes = reportMapper.getLastUsersWhoActedOnRnr(orderReportParam.getOrderId(), RnrStatus.IN_APPROVAL.name());
        if (!changes.isEmpty()) {
            result.put(IN_APPROVAL_BY, changes.get(0).getCreatedBy().getFirstName() + " " + changes.get(0).getCreatedBy().getLastName());
            result.put(IN_APPROVAL_DATE, dateFormat.format(changes.get(0).getCreatedDate()));
        }

        changes = reportMapper.getLastUsersWhoActedOnRnr(orderReportParam.getOrderId(), RnrStatus.APPROVED.name());
        if (!changes.isEmpty()) {
            result.put(APPROVED_BY, changes.get(0).getCreatedBy().getFirstName() + " " + changes.get(0).getCreatedBy().getLastName());
            result.put(APPROVED_DATE, dateFormat.format(changes.get(0).getCreatedDate()));
        }

        // register the facility related parameters here
        Facility currentFacility = reportLookupService.getFacilityForRnrId(orderReportParam.getOrderId());
        result.put(FACILITY_ADDRESS, currentFacility.getAddress1());
        result.put(PHONE, currentFacility.getMainPhone());
        result.put(FAX, currentFacility.getFax());
        result.put(DISTRICT, currentFacility.getGeographicZone().getName());
        if (currentFacility.getGeographicZone() != null) {
            result.put(REGION, currentFacility.getGeographicZone().getParent().getName());
        }
        return result;
    }
}
