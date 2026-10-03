package com.soybe.awakeningsystem.guild;

public enum GuildRole {
    LEADER("§6[Líder]", 5, true, true, true, true, true, true, true),
    OFFICER("§b[Oficial]", 4, true, true, false, false, true, true, true),
    VETERAN("§a[Veterano]", 3, true, false, false, false, true, false, false),
    MEMBER("§7[Miembro]", 2, false, false, false, false, true, false, false),
    RECRUIT("§8[Recluta]", 1, false, false, false, false, true, false, false);

    private final String label;
    private final int level;
    private final boolean canInvite;
    private final boolean canKick;
    private final boolean canManagePerks;
    private final boolean canDisband;
    private final boolean canDeposit;
    private final boolean canWithdraw;
    private final boolean canClaimTerritory;

    GuildRole(String label, int level, boolean canInvite, boolean canKick, boolean canManagePerks, boolean canDisband, boolean canDeposit, boolean canWithdraw, boolean canClaimTerritory) {
        this.label = label;
        this.level = level;
        this.canInvite = canInvite;
        this.canKick = canKick;
        this.canManagePerks = canManagePerks;
        this.canDisband = canDisband;
        this.canDeposit = canDeposit;
        this.canWithdraw = canWithdraw;
        this.canClaimTerritory = canClaimTerritory;
    }

    public String getLabel() { return label; }
    public int getLevel() { return level; }
    public boolean canInvite() { return canInvite; }
    public boolean canKick() { return canKick; }
    public boolean canManagePerks() { return canManagePerks; }
    public boolean canDisband() { return canDisband; }
    public boolean canDeposit() { return canDeposit; }
    public boolean canWithdraw() { return canWithdraw; }
    public boolean canClaimTerritory() { return canClaimTerritory; }
}
