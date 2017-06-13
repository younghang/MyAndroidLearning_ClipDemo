package com.example.yanghang.clipboard.ListPackage.CatalogueList;

/**
 * Created by young on 2017/2/26.
 */

public class CatalogueInfos {
    String Catalogue="";
    String CatalogueDescription="";

    public CatalogueInfos(String catalogue, String  catalogueDescription) {
        CatalogueDescription = catalogueDescription;
        Catalogue = catalogue;
    }

    public String getCatalogue() {
        return Catalogue;
    }

    public void setCatalogue(String catalogue) {
        Catalogue = catalogue;
    }

    public String getCatalogueDescription() {
        return CatalogueDescription;
    }

    public void setCatalogueDescription(String catalogueDescription) {
        CatalogueDescription = catalogueDescription;
    }
}
