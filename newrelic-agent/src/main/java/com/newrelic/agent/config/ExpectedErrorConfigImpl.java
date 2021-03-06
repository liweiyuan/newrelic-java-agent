/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.newrelic.agent.config;

public class ExpectedErrorConfigImpl implements ExpectedErrorConfig {

    private final String exceptionClassName;
    private final String exceptionMessage;

    public ExpectedErrorConfigImpl(String exceptionClassName, String exceptionMessage) {
        this.exceptionClassName = exceptionClassName;
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public String getErrorClass() {
        return exceptionClassName;
    }

    @Override
    public String getErrorMessage() {
        return exceptionMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExpectedErrorConfigImpl that = (ExpectedErrorConfigImpl) o;
        if (!exceptionClassName.equals(that.exceptionClassName)) {
            return false;
        }
        return exceptionMessage != null ? exceptionMessage.equals(that.exceptionMessage) : that.exceptionMessage == null;
    }

    @Override
    public int hashCode() {
        int result = exceptionClassName.hashCode();
        result = 31 * result + (exceptionMessage != null ? exceptionMessage.hashCode() : 0);
        return result;
    }

}
