package org.haobtc.wallet.exception;

//
// Created by liyan on 2020/5/24.
//
public enum BixinExceptions {

   PIN_INVALID("PIN invalid", 7),
   UN_PAIRABLE("BaseException: BiXin cannot pair with your Trezor.", 8),
   TRANSACTION_FORMAT_ERROR("BaseException: failed to recognize transaction encoding for txt: craft fury pig target diagram ...", 9),
   PIN_OPERATION_TIMEOUT("BaseException: waiting pin timeout", 10),
   PASSPHRASE_OPERATION_TIMEOUT("BaseException: waiting passphrase timeout", 11),
   BLE_RESPONSE_READ_TIMEOUT("read ble response timeout", 12);


   private final String message;
   private final int code;
   BixinExceptions(String message, int code) {
      this.message = message;
      this.code = code;
   }

   public int getCode() {
      return code;
   }

   public String getMessage() {
      return message;
   }
}