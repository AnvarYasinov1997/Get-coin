package com.getIn.getCoin.dtos;

import java.util.List;

public class InitializeDto {

    private List<UserDto> userDtoList;

    private List<BlockDto> blockDtoList;

    private List<TransactionOutputDto> UTXOsDtoList;

    public InitializeDto() {
    }

    public InitializeDto(final List<UserDto> userDtoList,
                         final List<BlockDto> blockDtoList,
                         final List<TransactionOutputDto> UTXOsDtoList) {
        this.userDtoList = userDtoList;
        this.blockDtoList = blockDtoList;
        this.UTXOsDtoList = UTXOsDtoList;
    }

    public List<UserDto> getUserDtoList() {
        return userDtoList;
    }

    public void setUserDtoList(List<UserDto> userDtoList) {
        this.userDtoList = userDtoList;
    }

    public List<BlockDto> getBlockDtoList() {
        return blockDtoList;
    }

    public void setBlockDtoList(List<BlockDto> blockDtoList) {
        this.blockDtoList = blockDtoList;
    }

    public List<TransactionOutputDto> getUTXOsDtoList() {
        return UTXOsDtoList;
    }

    public void setUTXOsDtoList(List<TransactionOutputDto> UTXOsDtoList) {
        this.UTXOsDtoList = UTXOsDtoList;
    }
}
