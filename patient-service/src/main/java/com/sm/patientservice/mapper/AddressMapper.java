package com.sm.patientservice.mapper;

public class AddressMapper {
    
    public static com.sm.patientservice.model.dto.Address toDto(com.sm.patientservice.model.Address address) {
        if (address == null) {
            return null;
        }
        com.sm.patientservice.model.dto.Address dto = new com.sm.patientservice.model.dto.Address();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        
        return dto;
    }

    public static com.sm.patientservice.model.Address toEntity(com.sm.patientservice.model.dto.Address addressDto) {
        if (addressDto == null) {
            return null;
        }
        com.sm.patientservice.model.Address entity = new com.sm.patientservice.model.Address();
        entity.setStreet(addressDto.getStreet());
        entity.setCity(addressDto.getCity());
        entity.setState(addressDto.getState());
        entity.setZipCode(addressDto.getZipCode());
        entity.setCountry(addressDto.getCountry());
        return entity;
    }

}
