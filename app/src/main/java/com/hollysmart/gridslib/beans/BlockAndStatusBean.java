package com.hollysmart.gridslib.beans;

import java.io.Serializable;

public class BlockAndStatusBean implements Serializable {

   private BlockBean block;
   private String fdStatus;
   private String blockProperty;
   private String nullAddFlag;

    public BlockBean getBlock() {
        return block;
    }

    public void setBlock(BlockBean block) {
        this.block = block;
    }

    public String getFdStatus() {
        return fdStatus;
    }

    public void setFdStatus(String fdStatus) {
        this.fdStatus = fdStatus;
    }

    public String getBlockProperty() {
        return blockProperty;
    }

    public void setBlockProperty(String blockProperty) {
        this.blockProperty = blockProperty;
    }

    public String getNullAddFlag() {
        return nullAddFlag;
    }

    public void setNullAddFlag(String nullAddFlag) {
        this.nullAddFlag = nullAddFlag;
    }
}
