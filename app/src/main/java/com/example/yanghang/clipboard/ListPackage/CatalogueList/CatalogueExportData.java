package com.example.yanghang.clipboard.ListPackage.CatalogueList;

/**
 * Created by young on 2017/11/23.
 */

public class CatalogueExportData {
    String catalogueName;
    boolean isExport=true;

    public CatalogueExportData(String catalogueName, boolean isExport) {
        this.catalogueName = catalogueName;
        this.isExport = isExport;
    }

    public CatalogueExportData() {
    }

    public String getCatalogueName() {
        return catalogueName;
    }

    public void setCatalogueName(String catalogueName) {
        this.catalogueName = catalogueName;
    }

    public boolean isExport() {
        return isExport;
    }

    public void setExport(boolean export) {
        isExport = export;
    }
}
