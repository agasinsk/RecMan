package com.agasinsk.recman.microsoft.graph;

import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalException;

public interface MicrosoftAuthenticationCallback {
    void onMicrosoftAuthenticationSuccess(AuthenticationResult authenticationResult);
    void onMicrosoftAuthenticationError(MsalException exception);
    void onMicrosoftAuthenticationCancel();
}
