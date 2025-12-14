package com.dbt.chatease.Service;

import com.dbt.chatease.Utils.Result;

public interface UserContactService {


    Result searchFriendOrGroup(String contactId);

    Result getMyContacts(Integer contactType);

    Result getContactDetail(String contactId);

    Result deleteContact(String contactId);

    Result blockContact(String contactId);
}