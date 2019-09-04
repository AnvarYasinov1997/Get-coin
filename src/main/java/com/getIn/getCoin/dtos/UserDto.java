package com.getIn.getCoin.dtos;

public class UserDto {

    private String userId;

    private String port;

    private String ipAddress;

    public UserDto() {
    }

    public UserDto(final String userId, final String port, final String ipAddress) {
        this.userId = userId;
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public static InternetAddressBuilder builder() {
        return new InternetAddressBuilder();
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(this.userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != this.getClass()) return false;
        UserDto userDto = (UserDto) obj;
        return this.userId.equals(userDto.userId);
    }

    public static class InternetAddressBuilder {

        private String userId;

        private String port;

        private String ipAddress;

        public InternetAddressBuilder userId(final String userId) {
            this.userId = userId;
            return this;
        }

        public InternetAddressBuilder port(final String port) {
            this.port = port;
            return this;
        }

        public InternetAddressBuilder ipAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public UserDto build() {
            return new UserDto(userId, port, ipAddress);
        }

    }
}
