/**
 * API
 * Create API keys in your profile and use public API key as username and secret as password to authorize.
 *
 * The version of the OpenAPI document: 2.19.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.mycelium.bequant.remote.trading.model


import com.squareup.moshi.Json

/**
 *
 * @param code
 * @param message
 * @param description
 */

data class ErrorError(
        @Json(name = "code")
        val code: ErrorError.Code,
        @Json(name = "message")
        val message: ErrorError.Message,
        @Json(name = "description")
        val description: kotlin.String? = null
) {

    /**
     *
     * Values: _500,_504,_503,_400,_1001,_1002,_2001,_2002,_10001
     */

    enum class Code(val value: kotlin.Int) {
        @Json(name = "500")
        _500(500),
        @Json(name = "504")
        _504(504),
        @Json(name = "503")
        _503(503),
        @Json(name = "400")
        _400(400),
        @Json(name = "1001")
        _1001(1001),
        @Json(name = "1002")
        _1002(1002),
        @Json(name = "2001")
        _2001(2001),
        @Json(name = "2002")
        _2002(2002),
        @Json(name = "10001")
        _10001(10001);
    }

    /**
     *
     * Values: internalServerError,gatewayTimeout,serviceUnavailable,validationError,authorisationRequired,authorisationFailed,noSuchSymbol,noSuchCurrency,insufficientFunds
     */

    enum class Message(val value: kotlin.String) {
        @Json(name = "Internal Server Error")
        internalServerError("Internal Server Error"),
        @Json(name = "Gateway Timeout")
        gatewayTimeout("Gateway Timeout"),
        @Json(name = "Service Unavailable")
        serviceUnavailable("Service Unavailable"),
        @Json(name = "Validation error")
        validationError("Validation error"),
        @Json(name = "Authorisation required")
        authorisationRequired("Authorisation required"),
        @Json(name = "Authorisation failed")
        authorisationFailed("Authorisation failed"),
        @Json(name = "No such symbol")
        noSuchSymbol("No such symbol"),
        @Json(name = "No such currency")
        noSuchCurrency("No such currency"),
        @Json(name = "Insufficient funds")
        insufficientFunds("Insufficient funds");
    }
}

