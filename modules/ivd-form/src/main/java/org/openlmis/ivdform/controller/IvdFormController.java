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
package org.openlmis.ivdform.controller;

import org.openlmis.core.service.FacilityService;
import org.openlmis.core.service.ProgramService;
import org.openlmis.core.service.UserService;
import org.openlmis.core.web.OpenLmisResponse;
import org.openlmis.core.web.controller.BaseController;
import org.openlmis.ivdform.domain.reports.VaccineReport;
import org.openlmis.ivdform.service.IvdFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
@RequestMapping(value = "/vaccine/report/")
public class IvdFormController extends BaseController {

  private static final String PERIODS = "periods";
  private static final String REPORT = "report";
  private static final String PENDING_SUBMISSIONS = "pending_submissions";

  @Autowired
  IvdFormService service;

  @Autowired
  ProgramService programService;

  @Autowired
  UserService userService;

  @Autowired
  FacilityService facilityService;


  @RequestMapping(value = "periods/{facilityId}/{programId}", method = RequestMethod.GET)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'CREATE_IVD')")
  public ResponseEntity<OpenLmisResponse> getPeriods(@PathVariable Long facilityId, @PathVariable Long programId) {
    return OpenLmisResponse.response(PERIODS, service.getPeriodsFor(facilityId, programId, new Date()));
  }

  @RequestMapping(value = "view-periods/{facilityId}/{programId}", method = RequestMethod.GET)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'VIEW_IVD')")
  public ResponseEntity<OpenLmisResponse> getViewPeriods(@PathVariable Long facilityId, @PathVariable Long programId) {
    return OpenLmisResponse.response(PERIODS, service.getReportedPeriodsFor(facilityId, programId));
  }

  @RequestMapping(value = "initialize/{facilityId}/{programId}/{periodId}")
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'CREATE_IVD')")
  public ResponseEntity<OpenLmisResponse> initialize(
      @PathVariable Long facilityId,
      @PathVariable Long programId,
      @PathVariable Long periodId,
      HttpServletRequest request
  ) {
    return OpenLmisResponse.response(REPORT, service.initialize(facilityId, programId, periodId, loggedInUserId(request)));
  }

  @RequestMapping(value = "get/{id}.json", method = RequestMethod.GET)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'CREATE_IVD, VIEW_IVD, APPROVE_IVD')")
  public ResponseEntity<OpenLmisResponse> getReport(@PathVariable Long id) {
    return OpenLmisResponse.response(REPORT, service.getById(id));
  }

  @RequestMapping(value = "save")
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'CREATE_IVD')")
  public ResponseEntity<OpenLmisResponse> save(@RequestBody VaccineReport report, HttpServletRequest request) {
    service.save(report, loggedInUserId(request));
    return OpenLmisResponse.response(REPORT, report);
  }

  @RequestMapping(value = "submit")
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'CREATE_IVD')")
  public ResponseEntity<OpenLmisResponse> submit(@RequestBody VaccineReport report, HttpServletRequest request) {
    service.submit(report, loggedInUserId(request));
    return OpenLmisResponse.response(REPORT, report);
  }

  @RequestMapping(value = "approval-pending")
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'APPROVE_IVD')")
  public ResponseEntity<OpenLmisResponse> getPendingFormsForApproval(@RequestParam("program") Long programId, HttpServletRequest request){
    return OpenLmisResponse.response(PENDING_SUBMISSIONS, service.getApprovalPendingForms(this.loggedInUserId(request), programId));
  }

  @RequestMapping(value = "approve")
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'APPROVE_IVD')")
  public ResponseEntity<OpenLmisResponse> approve(@RequestBody VaccineReport report, HttpServletRequest request) {
    service.approve(report, loggedInUserId(request));
    return OpenLmisResponse.response(REPORT, report);
  }

  @RequestMapping(value = "reject")
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'APPROVE_IVD')")
  public ResponseEntity<OpenLmisResponse> reject(@RequestBody VaccineReport report, HttpServletRequest request) {
    service.reject(report, loggedInUserId(request));
    return OpenLmisResponse.response(REPORT, report);
  }

}
