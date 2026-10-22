package dev.avatrix;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {
    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private static final String[] CATS = {"Combat", "Visuals", "World", "Movement", "Player"};
    private static final String[] SETS = {"Configs", "Friends", "Theme", "Settings"};
    private static final int W = 520, H = 320, SB = 130, BAR_W = 140, BAR_H = 14;

    private final List<Hit> hits = new ArrayList<>();
    private final List<int[]> bars = new ArrayList<>(); // {x, y, id}
    private String page = "Configs";
    private boolean cloud;
    private int x0, y0, scroll, dragging;

    public ClickGui() { super(Text.literal("Avatrix")); }

    @Override public boolean shouldPause() { return false; }

    private void text(DrawContext c, String s, int x, int y, int col) {
        c.drawText(textRenderer, s, x, y, col, false);
    }

    private void button(DrawContext c, int x, int y, int w, int h, String label, int bg, Runnable r) {
        c.fill(x, y, x + w, y + h, bg);
        c.drawText(textRenderer, label, x + (w - textRenderer.getWidth(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF, false);
        hits.add(new Hit(x, y, w, h, r));
    }

    private void toggle(DrawContext c, int x, int y, boolean on, int acc, Runnable r) {
        c.fill(x, y, x + 34, y + 16, on ? acc : 0xFF3D3E46);
        c.fill(x + (on ? 20 : 2), y + 2, x + (on ? 32 : 14), y + 14, 0xFFFFFFFF);
        hits.add(new Hit(x, y, 34, 16, r));
    }

    private void hueBar(DrawContext c, int x, int y, int id, int rgb) {
        for (int i = 0; i < BAR_W; i++) {
            c.fill(x + i, y, x + i + 1, y + BAR_H, 0xFF000000 | Color.HSBtoRGB(i / (float) BAR_W, 0.7f, 1f));
        }
        float hue = Color.RGBtoHSB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, null)[0];
        int mx = x + Math.round(hue * (BAR_W - 1));
        c.fill(mx - 1, y - 2, mx + 1, y + BAR_H + 2, 0xFFFFFFFF);
        c.fill(x + BAR_W + 8, y, x + BAR_W + 8 + BAR_H, y + BAR_H, 0xFF000000 | rgb);
        bars.add(new int[]{x, y, id});
    }

    private void applyBar(double mx) {
        for (int[] b : bars) {
            if (b[2] != dragging) continue;
            float h = (float) Math.max(0, Math.min(1, (mx - b[0]) / (BAR_W - 1)));
            int rgb = Color.HSBtoRGB(h, 0.7f, 1f) & 0xFFFFFF;
            if (dragging == 1) State.espColor = rgb; else State.accent = rgb;
        }
    }

    private int nav(DrawContext c, String group, String[] items, int y) {
        text(c, group, x0 + 10, y, 0xFF8A8B94);
        y += 14;
        for (String it : items) {
            boolean on = it.equals(page);
            if (on) c.fill(x0 + 6, y - 3, x0 + SB - 6, y + 13, 0xFF34304A);
            text(c, it, x0 + 16, y, on ? 0xFFFFFFFF : 0xFFC9C9D0);
            hits.add(new Hit(x0 + 6, y - 3, SB - 12, 16, () -> { page = it; scroll = 0; }));
            y += 18;
        }
        return y;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        hits.clear();
        bars.clear();
        x0 = (width - W) / 2;
        y0 = (height - H) / 2;
        int acc = 0xFF000000 | State.accent;

        c.fill(0, 0, width, height, 0x80000000);
        c.fill(x0, y0, x0 + W, y0 + H, 0xFF1C1D1B);
        c.fill(x0, y0, x0 + SB, y0 + H, 0xFF232422);
        c.drawBorder(x0, y0, W, H, 0xFF2F302E);

        c.fill(x0 + 8, y0 + 8, x0 + SB - 8, y0 + 36, 0xFF1E4FA8);
        c.drawText(textRenderer, "Avatrix", x0 + 18, y0 + 18, 0xFFFFFFFF, true);

        int y = nav(c, "Client categories", CATS, y0 + 48);
        nav(c, "Settings", SETS, y + 8);

        int cx = x0 + SB + 16, cy = y0 + 16, cw = W - SB - 32;
        switch (page) {
            case "Configs" -> configs(c, cx, cy, cw, acc);
            case "Visuals" -> visuals(c, cx, cy, cw, acc);
            case "Theme" -> theme(c, cx, cy, cw);
            default -> {
                text(c, page, cx, cy + 4, 0xFFFFFFFF);
                text(c, "No modules.", cx, cy + 28, 0xFF8A8B94);
            }
        }
    }

    private void configs(DrawContext c, int cx, int cy, int cw, int acc) {
        text(c, "Configs", cx, cy + 4, 0xFFFFFFFF);
        button(c, cx + 60, cy, 44, 16, "Local", !cloud ? acc : 0xFF2A2B29, () -> cloud = false);
        button(c, cx + 110, cy, 44, 16, "Cloud", cloud ? acc : 0xFF2A2B29, () -> cloud = true);
        button(c, cx + cw - 20, cy, 20, 16, "+", acc, () -> ConfigManager.save(ConfigManager.nextName()));

        int y = cy + 28;
        if (cloud) { text(c, "No cloud configs yet.", cx, y, 0xFF8A8B94); return; }
        List<String> names = ConfigManager.list();
        if (names.isEmpty()) { text(c, "No local configs. Press + to save the current settings.", cx, y, 0xFF8A8B94); return; }

        scroll = Math.max(0, Math.min(scroll, Math.max(0, names.size() - 4)));
        for (int i = scroll; i < Math.min(names.size(), scroll + 4); i++) {
            String n = names.get(i);
            c.fill(cx, y, cx + cw, y + 50, 0xFF232422);
            c.drawBorder(cx, y, cw, 50, 0xFF4A4466);
            text(c, n, cx + 8, y + 8, 0xFFFFFFFF);
            if (n.equals(ConfigManager.loaded)) {
                int tx = cx + 16 + textRenderer.getWidth(n);
                c.fill(tx, y + 6, tx + 42, y + 18, acc);
                text(c, "Loaded", tx + 5, y + 8, 0xFFFFFFFF);
            }
            text(c, ConfigManager.date(n), cx + 8, y + 30, 0xFF8A8B94);
            button(c, cx + cw - 100, y + 28, 16, 16, "x", 0xFF5A2E2E, () -> ConfigManager.delete(n));
            button(c, cx + cw - 76, y + 28, 68, 16, "Load", acc, () -> ConfigManager.load(n));
            y += 56;
        }
    }

    private void visuals(DrawContext c, int cx, int cy, int cw, int acc) {
        text(c, "Visuals", cx, cy + 4, 0xFFFFFFFF);
        int y = cy + 28;
        c.fill(cx, y, cx + cw, y + 64, 0xFF232422);
        c.drawBorder(cx, y, cw, 64, 0xFF3A3B44);
        text(c, "Target ESP", cx + 10, y + 8, 0xFFFFFFFF);
        text(c, "Outlines your target. Visible only in line of sight.", cx + 10, y + 22, 0xFF8A8B94);
        toggle(c, cx + cw - 44, y + 6, State.targetEsp, acc, () -> State.targetEsp = !State.targetEsp);
        text(c, "Color", cx + 10, y + 44, 0xFF8A8B94);
        hueBar(c, cx + 50, y + 42, 1, State.espColor);
    }

    private void theme(DrawContext c, int cx, int cy, int cw) {
        text(c, "Theme", cx, cy + 4, 0xFFFFFFFF);
        int y = cy + 28;
        c.fill(cx, y, cx + cw, y + 48, 0xFF232422);
        c.drawBorder(cx, y, cw, 48, 0xFF3A3B44);
        text(c, "Accent color", cx + 10, y + 8, 0xFFFFFFFF);
        hueBar(c, cx + 10, y + 26, 2, State.accent);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int[] b : bars) {
                if (mx >= b[0] && mx < b[0] + BAR_W && my >= b[1] && my < b[1] + BAR_H) {
                    dragging = b[2];
                    applyBar(mx);
                    return true;
                }
            }
            for (Hit h : new ArrayList<>(hits)) {
                if (h.in(mx, my)) { h.action().run(); return true; }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != 0) { applyBar(mx); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = 0;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll -= (int) Math.signum(v);
        return true;
    }
}
