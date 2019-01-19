/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.agasinsk.recman.microsoft.graph;

class Constants {
    // The Microsoft Graph delegated permissions that you set in the application
    // registration portal must match these scope values.
    // Update this constant with the scope (permission) values for your application:
    public static final String[] SCOPES = {"openid", "Files.ReadWrite", "Files.ReadWrite.All", "User.ReadBasic.All"};
}
