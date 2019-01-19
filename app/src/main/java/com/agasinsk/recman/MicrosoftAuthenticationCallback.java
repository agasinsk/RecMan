package com.agasinsk.recman;

import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalException;

interface MicrosoftAuthenticationCallback {
    void onMicrosoftAuthenticationSuccess(AuthenticationResult authenticationResult);
    void onMicrosoftAuthenticationError(MsalException exception);
    void onMicrosoftAuthenticationCancel();
}
