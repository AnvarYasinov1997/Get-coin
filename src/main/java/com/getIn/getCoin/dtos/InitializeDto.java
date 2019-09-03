package com.getIn.getCoin.dtos;

import java.util.List;

public class InitializeDto {

    private List<UserData> userDataList;

    private List<Block> blocks;

    private List<String> checkSums;

    public InitializeDto() {
    }

    public InitializeDto(final List<String> checkSums,
                         final List<Block> blocks) {
        this.blocks = blocks;
        this.checkSums = checkSums;
    }

    public InitializeDto(final List<UserData> userDataList,
                         final List<Block> blocks,
                         final List<String> checkSums) {
        this.userDataList = userDataList;
        this.blocks = blocks;
        this.checkSums = checkSums;
    }

    public List<UserData> getUserDataList() {
        return userDataList;
    }

    public void setUserDataList(List<UserData> userDataList) {
        this.userDataList = userDataList;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public List<String> getCheckSums() {
        return checkSums;
    }

    public void setCheckSums(List<String> checkSums) {
        this.checkSums = checkSums;
    }
}
