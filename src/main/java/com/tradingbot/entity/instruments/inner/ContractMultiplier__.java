
package com.tradingbot.entity.instruments.inner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContractMultiplier__ {

    @SerializedName("mantissa")
    @Expose
    private Long mantissa;
    @SerializedName("exponent")
    @Expose
    private Long exponent;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ContractMultiplier__() {
    }

    /**
     * 
     * @param mantissa
     * @param exponent
     */
    public ContractMultiplier__(Long mantissa, Long exponent) {
        super();
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    public Long getMantissa() {
        return mantissa;
    }

    public void setMantissa(Long mantissa) {
        this.mantissa = mantissa;
    }

    public ContractMultiplier__ withMantissa(Long mantissa) {
        this.mantissa = mantissa;
        return this;
    }

    public Long getExponent() {
        return exponent;
    }

    public void setExponent(Long exponent) {
        this.exponent = exponent;
    }

    public ContractMultiplier__ withExponent(Long exponent) {
        this.exponent = exponent;
        return this;
    }

}
