package com.shuzi.managementplatform.domain.enums;

public enum ParentBindingType {
    AUTO,
    MANUAL,
    AUTO_MANUAL;

    public ParentBindingType mergeAutomatic() {
        return this == MANUAL ? AUTO_MANUAL : this;
    }

    public ParentBindingType removeAutomatic() {
        return this == AUTO_MANUAL ? MANUAL : this;
    }

    public ParentBindingType mergeManual() {
        return this == AUTO ? AUTO_MANUAL : this;
    }

    public ParentBindingType removeManual() {
        return this == AUTO_MANUAL ? AUTO : null;
    }
}
