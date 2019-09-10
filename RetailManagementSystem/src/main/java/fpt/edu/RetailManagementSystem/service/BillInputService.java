package fpt.edu.RetailManagementSystem.service;

import fpt.edu.RetailManagementSystem.service.dto.BillInputDTO;
import fpt.edu.RetailManagementSystem.service.dto.BillInputDetailDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillInputService {
    List<BillInputDetailDTO> create(List<BillInputDetailDTO> billDetailDTOS, String code, Integer accountID, float tax, Integer supllier);
    List<BillInputDTO> getAllBill();
    List<BillInputDetailDTO> getAllProductOfBill(Integer billID);
    Boolean updateStatus(Integer id);
    Boolean updateIsPaid( Integer id);
}
