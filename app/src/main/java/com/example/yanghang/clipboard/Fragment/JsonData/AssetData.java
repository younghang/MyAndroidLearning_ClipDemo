package com.example.yanghang.clipboard.Fragment.JsonData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssetData {
    public static final String CATALOGUE_NAME = "资产";

    private static final String DEFAULT_GROUP = "国内账户";
    private static final String RECEIVABLE_GROUP = "应收/借出";
    private static final String NOTE_GROUP = "备注/不计入资产";
    private static final BigDecimal WAN = new BigDecimal("10000");
    private static final Pattern TOTAL_PATTERN = Pattern.compile("^(?:合计|总计|总资产|资产总计)\\s*[:：]?\\s*([+-]?[0-9,]+(?:\\.[0-9]+)?\\s*(?:w|W|万)?)\\s*$");
    private static final Pattern ITEM_PATTERN = Pattern.compile("^(.*?)[\\s:：]*([+-]?[0-9,]+(?:\\.[0-9]+)?\\s*(?:w|W|万)?)\\s*$");

    private int version = 1;
    private List<AssetGroup> groups;
    @JSONField(serialize = false)
    private List<String> warnings;

    public AssetData() {
    }

    public static AssetData parse(String content) {
        if (content == null || content.trim().equals("")) {
            return new AssetData();
        }
        String text = content.trim();
        if (text.startsWith("{")) {
            try {
                AssetData data = JSON.parseObject(text, AssetData.class);
                if (data != null) {
                    data.normalize();
                    return data;
                }
            } catch (Exception ignored) {
            }
        }
        return parseLegacyText(content);
    }

    public String buildSimpleContent() {
        normalize();
        StringBuilder builder = new StringBuilder();
        BigDecimal includedTotal = BigDecimal.ZERO;

        for (int i = 0; i < groups.size(); i++) {
            AssetGroup group = groups.get(i);
            includedTotal = includedTotal.add(group.getIncludedSubtotal());
        }

        builder.append("资产总计 ").append(formatMoney(includedTotal));

        appendGroupSummaries(builder, true);
        appendGroupSummaries(builder, false);

        appendGroupDetails(builder, true);
        appendGroupDetails(builder, false);
        return builder.toString();
    }

    private void appendGroupSummaries(StringBuilder builder, boolean includeInTotal) {
        for (int i = 0; i < groups.size(); i++) {
            AssetGroup group = groups.get(i);
            if (group.isIncludeInTotal() != includeInTotal || group.getItems().size() == 0) {
                continue;
            }
            builder.append("\n").append(group.getName()).append(" ").append(formatMoney(group.getSubtotal()));
            if (!group.isIncludeInTotal()) {
                builder.append(" (不计入)");
            }
        }
    }

    private void appendGroupDetails(StringBuilder builder, boolean includeInTotal) {
        for (int i = 0; i < groups.size(); i++) {
            AssetGroup group = groups.get(i);
            if (group.isIncludeInTotal() != includeInTotal || group.getItems().size() == 0) {
                continue;
            }
            builder.append("\n\n").append(group.getName());
            for (int j = 0; j < group.getItems().size(); j++) {
                AssetItem item = group.getItems().get(j);
                builder.append("\n").append(item.getName()).append("  ").append(formatMoney(item.getAmountValue()));
                if (group.isIncludeInTotal() && !item.isIncludeInTotal()) {
                    builder.append("  不计入");
                }
            }
        }
    }

    private static AssetData parseLegacyText(String content) {
        AssetData data = new AssetData();
        data.normalize();
        String currentGroupName = DEFAULT_GROUP;
        int unnamedDomesticIndex = 1;
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i].trim();
            if (line.equals("")) {
                continue;
            }

            Matcher totalMatcher = TOTAL_PATTERN.matcher(line);
            if (totalMatcher.matches()) {
                BigDecimal total = parseMoney(totalMatcher.group(1));
                if (total != null) {
                    AssetGroup group = data.getOrCreateGroup(currentGroupName, isIncludedGroup(currentGroupName));
                    group.setDeclaredTotal(formatMoney(total));
                }
                continue;
            }

            Matcher itemMatcher = ITEM_PATTERN.matcher(line);
            if (itemMatcher.matches()) {
                String name = trimName(itemMatcher.group(1));
                BigDecimal amount = parseMoney(itemMatcher.group(2));
                if (amount != null) {
                    String groupName;
                    if (name.equals("")) {
                        name = "国内" + unnamedDomesticIndex;
                        unnamedDomesticIndex++;
                        groupName = DEFAULT_GROUP;
                    } else {
                        groupName = getGroupNameForItem(currentGroupName, name);
                    }
                    AssetGroup group = data.getOrCreateGroup(groupName, isIncludedGroup(groupName));
                    group.getItems().add(new AssetItem(name, formatMoney(amount), group.isIncludeInTotal()));
                    continue;
                }
            }

            currentGroupName = line;
            data.getOrCreateGroup(currentGroupName, isIncludedGroup(currentGroupName));
        }
        data.removeEmptyGroups();
        return data;
    }

    private static String getGroupNameForItem(String currentGroupName, String itemName) {
        if (isReceivable(itemName)) {
            return RECEIVABLE_GROUP;
        }
        if (isNoteOnly(itemName)) {
            return NOTE_GROUP;
        }
        if (currentGroupName == null || currentGroupName.trim().equals("")) {
            return DEFAULT_GROUP;
        }
        return currentGroupName;
    }

    private static boolean isReceivable(String name) {
        return name.startsWith("借给") || name.startsWith("借出") || name.contains("借给");
    }

    private static boolean isNoteOnly(String name) {
        return name.startsWith("给") && !isReceivable(name);
    }

    private static boolean isIncludedGroup(String groupName) {
        if (groupName == null) {
            return true;
        }
        return !groupName.contains("不计入") && !groupName.contains("备注");
    }

    private static BigDecimal parseMoney(String value) {
        if (value == null) {
            return null;
        }
        String moneyText = value.trim().replace(",", "");
        if (moneyText.equals("")) {
            return null;
        }
        boolean isWan = moneyText.endsWith("w") || moneyText.endsWith("W") || moneyText.endsWith("万");
        if (isWan) {
            moneyText = moneyText.substring(0, moneyText.length() - 1).trim();
        }
        try {
            BigDecimal money = new BigDecimal(moneyText);
            if (isWan) {
                money = money.multiply(WAN);
            }
            return money.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private static String trimName(String name) {
        if (name == null) {
            return "";
        }
        String result = name.trim();
        while (result.endsWith(":") || result.endsWith("：")) {
            result = result.substring(0, result.length() - 1).trim();
        }
        return result;
    }

    private static String formatMoney(BigDecimal money) {
        if (money == null) {
            return "0.00";
        }
        return money.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void normalize() {
        if (groups == null) {
            groups = new ArrayList<AssetGroup>();
        }
        if (warnings == null) {
            warnings = new ArrayList<String>();
        }
        for (int i = groups.size() - 1; i >= 0; i--) {
            AssetGroup group = groups.get(i);
            if (group == null) {
                groups.remove(i);
            } else {
                group.normalize();
            }
        }
    }

    private AssetGroup getOrCreateGroup(String name, boolean includeInTotal) {
        normalize();
        String groupName = name == null || name.trim().equals("") ? DEFAULT_GROUP : name.trim();
        for (int i = 0; i < groups.size(); i++) {
            AssetGroup group = groups.get(i);
            if (groupName.equals(group.getName())) {
                return group;
            }
        }
        AssetGroup group = new AssetGroup(groupName, includeInTotal);
        groups.add(group);
        return group;
    }

    private void removeEmptyGroups() {
        for (int i = groups.size() - 1; i >= 0; i--) {
            if (groups.get(i).getItems().size() == 0) {
                groups.remove(i);
            }
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<AssetGroup> getGroups() {
        normalize();
        return groups;
    }

    public void setGroups(List<AssetGroup> groups) {
        this.groups = groups;
    }

    public List<String> getWarnings() {
        normalize();
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public static class AssetGroup {
        private String name;
        private String currency;
        private boolean includeInTotal = true;
        private String declaredTotal;
        private List<AssetItem> items;

        public AssetGroup() {
        }

        public AssetGroup(String name, boolean includeInTotal) {
            this.name = name;
            this.includeInTotal = includeInTotal;
            this.declaredTotal = "";
            this.items = new ArrayList<AssetItem>();
        }

        private void normalize() {
            if (name == null || name.trim().equals("")) {
                name = DEFAULT_GROUP;
            }
            if (currency == null) {
                currency = "";
            }
            if (declaredTotal == null) {
                declaredTotal = "";
            }
            if (items == null) {
                items = new ArrayList<AssetItem>();
            }
            for (int i = items.size() - 1; i >= 0; i--) {
                AssetItem item = items.get(i);
                if (item == null) {
                    items.remove(i);
                } else {
                    item.normalize(includeInTotal);
                }
            }
        }

        public BigDecimal getSubtotal() {
            normalize();
            BigDecimal subtotal = BigDecimal.ZERO;
            for (int i = 0; i < items.size(); i++) {
                AssetItem item = items.get(i);
                subtotal = subtotal.add(item.getAmountValue());
            }
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        public BigDecimal getIncludedSubtotal() {
            normalize();
            BigDecimal subtotal = BigDecimal.ZERO;
            for (int i = 0; i < items.size(); i++) {
                AssetItem item = items.get(i);
                if (item.isIncludeInTotal()) {
                    subtotal = subtotal.add(item.getAmountValue());
                }
            }
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        public BigDecimal getExcludedSubtotal() {
            normalize();
            BigDecimal subtotal = BigDecimal.ZERO;
            for (int i = 0; i < items.size(); i++) {
                AssetItem item = items.get(i);
                if (!item.isIncludeInTotal()) {
                    subtotal = subtotal.add(item.getAmountValue());
                }
            }
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        public String getName() {
            normalize();
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCurrency() {
            normalize();
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public boolean isIncludeInTotal() {
            return includeInTotal;
        }

        public void setIncludeInTotal(boolean includeInTotal) {
            this.includeInTotal = includeInTotal;
        }

        public String getDeclaredTotal() {
            normalize();
            return declaredTotal;
        }

        public void setDeclaredTotal(String declaredTotal) {
            this.declaredTotal = declaredTotal;
        }

        public List<AssetItem> getItems() {
            normalize();
            return items;
        }

        public void setItems(List<AssetItem> items) {
            this.items = items;
        }
    }

    public static class AssetItem {
        private String id;
        private String name;
        private String amount;
        private boolean includeInTotal = true;
        private String note;

        public AssetItem() {
        }

        public AssetItem(String name, String amount, boolean includeInTotal) {
            this.name = name;
            this.amount = amount;
            this.includeInTotal = includeInTotal;
            this.note = "";
        }

        private void normalize(boolean defaultIncludeInTotal) {
            if (id == null) {
                id = "";
            }
            if (name == null) {
                name = "";
            }
            if (amount == null) {
                amount = "0.00";
            }
            if (note == null) {
                note = "";
            }
            if (!defaultIncludeInTotal) {
                includeInTotal = false;
            }
        }

        public BigDecimal getAmountValue() {
            BigDecimal money = parseMoney(amount);
            return money == null ? BigDecimal.ZERO : money;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            if (name == null) {
                name = "";
            }
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAmount() {
            if (amount == null) {
                amount = "0.00";
            }
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public boolean isIncludeInTotal() {
            return includeInTotal;
        }

        public void setIncludeInTotal(boolean includeInTotal) {
            this.includeInTotal = includeInTotal;
        }

        public String getNote() {
            if (note == null) {
                note = "";
            }
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
