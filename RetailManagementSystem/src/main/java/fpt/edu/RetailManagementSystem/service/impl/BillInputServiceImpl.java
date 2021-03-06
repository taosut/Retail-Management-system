package fpt.edu.RetailManagementSystem.service.impl;

import fpt.edu.RetailManagementSystem.persistent.entity.BillInput;
import fpt.edu.RetailManagementSystem.persistent.entity.BillInputDetail;
import fpt.edu.RetailManagementSystem.persistent.entity.Product;
import fpt.edu.RetailManagementSystem.persistent.repository.AccountRepository;
import fpt.edu.RetailManagementSystem.persistent.repository.BillInputDetailRepository;
import fpt.edu.RetailManagementSystem.persistent.repository.BillInputRepository;
import fpt.edu.RetailManagementSystem.persistent.repository.ProductRepository;
import fpt.edu.RetailManagementSystem.service.BillInputService;
import fpt.edu.RetailManagementSystem.service.dto.BillInputDTO;
import fpt.edu.RetailManagementSystem.service.dto.BillInputDetailDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BillInputServiceImpl implements BillInputService {
    private final BillInputRepository billInputRepository;
    private final BillInputDetailRepository billInputDetailRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    public BillInputServiceImpl(BillInputRepository billInputRepository, BillInputDetailRepository billInputDetailRepository, ProductRepository productRepository, AccountRepository accountRepository) {
        this.billInputRepository = billInputRepository;
        this.billInputDetailRepository = billInputDetailRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<BillInputDetailDTO> create(BillInputDetailDTO billDetailDTOS){
        BillInput bill =  billInputRepository.findByCode(billDetailDTOS.getBillCode());
            float total = productRepository.findByID(billDetailDTOS.getProductID()).getPrice()*billDetailDTOS.getQuantity();
       if( bill == null){
           bill = new BillInput();
           bill.setTimeCreated(new Date());
           bill.setTotal(total* billDetailDTOS.getTax());
           bill.setStatus(true);
           bill.setCode(billDetailDTOS.getBillCode());
           bill.setTax(billDetailDTOS.getTax());
           bill.setAccountID(billDetailDTOS.getAccountID());
           bill.setIsPaid(false);
           bill.setSupplier(billDetailDTOS.getSupplier());
           billInputRepository.save(bill);
       }else {
           bill.setTotal(bill.getTotal()+ total );
           bill.setTax(billDetailDTOS.getTax());
           bill.setSupplier(billDetailDTOS.getSupplier());
           bill.setAccountID(billDetailDTOS.getAccountID());
           bill = billInputRepository.saveAndFlush(bill);
       }

            int newQuantiTY = productRepository.findByID(billDetailDTOS.getProductID()).getQuantity()+billDetailDTOS.getQuantity();
            productRepository.updateQuantity(newQuantiTY, billDetailDTOS.getProductID());
            BillInputDetail billDetail = new BillInputDetail();
            billDetail.setStatus(true);
            billDetail.setQuantity(billDetailDTOS.getQuantity());
            billDetail.setProductID(billDetailDTOS.getProductID());
            billDetail.setBillID(bill.getId());
            billDetail.setUnit(billDetailDTOS.getUnit());
            billInputDetailRepository.save(billDetail);
        List<BillInputDetailDTO> result = getAllProductOfBill(bill.getId());
        return result;
    }

    @Override
    public List<BillInputDTO> getAllBill(){
        List<BillInput> bills = billInputRepository.findAllByIsDelete();
        List<BillInputDTO> billDTOS = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (BillInput b : bills ) {
            BillInputDTO billDTO = modelMapper.map(b, BillInputDTO.class);
            billDTOS.add(billDTO);
        }
        return billDTOS;
    }

    @Override
    public List<BillInputDetailDTO> getBillDetailByCode(String code){
        BillInput bill =  billInputRepository.findByCode(code);
        List<BillInputDetailDTO> result = getAllProductOfBill(bill.getId());
        return result;
    }
    //List<BillInputDetailDTO>
    @Override
    public List<BillInputDetailDTO> getAllProductOfBill(Integer billID){
        List<BillInputDetail> billDetails = billInputDetailRepository.findAllByBillID(billID);
        List<BillInputDetailDTO> billDetailDTOS = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (BillInputDetail b : billDetails ) {
            Product product = modelMapper.map(productRepository.findByID(b.getProductID()), Product.class);
            BillInputDetailDTO billDetailDTO = modelMapper.map(b, BillInputDetailDTO.class);
            billDetailDTO.setCode(product.getCode());
            billDetailDTO.setPrice(product.getPrice() * b.getQuantity());
            billDetailDTO.setName(product.getName());
            billDetailDTOS.add(billDetailDTO);
        }
        return billDetailDTOS;
    }

    @Override
    public Boolean updateStatus(Integer id){
        Optional.ofNullable(billInputRepository.findById(id)).orElseThrow(() ->new EntityNotFoundException());
        if(billInputRepository.findBillByID(id).getStatus())
            billInputRepository.deleteByID(id, false);
        else
            billInputRepository.deleteByID(id, true);
        return true;
    }

    @Override
    public Boolean updateIsPaid( Integer id){
        Optional.ofNullable(billInputRepository.findById(id)).orElseThrow(() ->new EntityNotFoundException());
        if(billInputRepository.findBillByID(id).getIsPaid())
            billInputRepository.updateIsPaid(id, false);
        else
            billInputRepository.updateIsPaid(id, true);
        return true;
    }
}
