package com.example.yanghang.clipboard.ListPackage.AccountList;

import java.util.List;

/**
 * Created by young on 2017/11/25.
 */

public class AccountCatalogue {
    private String catalogueName;
    private List<AccountCatalogue> subCatalogue=null;

    public String getCatalogueName() {
        return catalogueName;
    }

    public void setCatalogueName(String catalogueName) {
        this.catalogueName = catalogueName;
    }

    public List<AccountCatalogue> getSubCatalogue() {
        return subCatalogue;
    }

    public void setSubCatalogue(List<AccountCatalogue> subCatalogue) {
        this.subCatalogue = subCatalogue;
    }
    public void addSubCatalogue(AccountCatalogue accountCatalogue)
    {
        this.subCatalogue.add(accountCatalogue);
    }
    public void addSubCatalogueName(String catalogue)
    {
        if (subCatalogue == null) {
            return;
        }
        this.subCatalogue.add(new AccountCatalogue(catalogue));
    }
    public void editSubCatalogueName(int pos,String catalogue)
    {
        this.subCatalogue.set(pos, new AccountCatalogue(catalogue));
    }
    public String getSubCatalogueName(int pos)
    {
        return this.subCatalogue.get(pos).getCatalogueName();
    }


    public AccountCatalogue() {
    }

    public AccountCatalogue(String catalogueName) {
        this.catalogueName = catalogueName;
    }
}
