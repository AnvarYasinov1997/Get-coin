package com.getIn.getCoin.dtos;

import java.util.List;

public class DeletedNodesDto {

    private List<String> deletedUserIds;

    public DeletedNodesDto() {
    }

    public DeletedNodesDto(List<String> deletedUserIds) {
        this.deletedUserIds = deletedUserIds;
    }

    public List<String> getDeletedUserIds() {
        return deletedUserIds;
    }

    public void setDeletedUserIds(List<String> deletedUserIds) {
        this.deletedUserIds = deletedUserIds;
    }
}
