package qr_ordering_system.service;

import java.util.Locale;

import qr_ordering_system.exception.BadRequestException;
import qr_ordering_system.model.CurrencyCode;

public final class RestaurantCurrencySupport {

    private RestaurantCurrencySupport() {
    }

    public static CurrencyCode resolveCurrencyCode(String rawCurrencyCode) {
        if (rawCurrencyCode == null || rawCurrencyCode.isBlank()) {
            return CurrencyCode.LKR;
        }

        try {
            return CurrencyCode.valueOf(rawCurrencyCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid currency code. Supported currencies: LKR, USD");
        }
    }

    public static String toApiValue(CurrencyCode currencyCode) {
        return currencyCode != null ? currencyCode.name() : CurrencyCode.LKR.name();
    }
}
