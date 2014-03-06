/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */
package org.openlmis.core.repository;

import org.openlmis.core.domain.DeliveryZoneWarehouse;
import org.openlmis.core.repository.mapper.DeliveryZoneWarehouseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * This is Repository class for DeliveryZoneWarehouse related database operations.
 */

@Repository
public class DeliveryZoneWarehouseRepository {

  @Autowired
  private DeliveryZoneWarehouseMapper mapper;

  public void insert(DeliveryZoneWarehouse warehouse) {
    mapper.insert(warehouse);
  }

  public void update(DeliveryZoneWarehouse warehouse) {
    mapper.update(warehouse);
  }

  public DeliveryZoneWarehouse getByDeliveryZoneCodeAndWarehouseCode(String deliveryZoneCode, String warehouseCode) {
    return mapper.getByDeliveryZoneCodeAndWarehouseCode(deliveryZoneCode, warehouseCode);
  }
}
