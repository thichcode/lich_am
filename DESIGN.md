# DESIGN.md

# Vietnamese Lunar Calendar

## UI/UX Design Specification

Version: 1.0

------------------------------------------------------------------------

# Design Philosophy

This application is NOT a typical Android utility app.

It should feel like a traditional Vietnamese desk calendar (Lịch Bloc)
redesigned for smartphones.

Primary users: - Elderly people (60+) - Parents - People who check the
lunar calendar every morning

The user should understand today's information within 3 seconds after
opening the app.

Everything important must be visible on the Home screen.

-   No advertisements
-   No clutter
-   No unnecessary animations
-   Offline-first

------------------------------------------------------------------------

# Visual Style

Theme: - Modern + Traditional Vietnamese - Clean - Peaceful -
Trustworthy - Premium

Background: - Pure white (#FFFFFF)

Cards: - Very light gray (#FAFAFA) - Rounded corners: 18dp - Very subtle
elevation

No gradients. No wallpapers. No decorative background behind the date.

------------------------------------------------------------------------

# Color Palette

Primary: #C62828 Success: #2E7D32 Danger: #D32F2F Accent: #F9A825
Background: #FFFFFF Card: #FAFAFA Divider: #EAEAEA Text Primary: #202124
Text Secondary: #5F6368

------------------------------------------------------------------------

# Typography

-   Today's number: 80sp Bold
-   Weekday: 34sp Bold
-   Lunar date: 24sp
-   Section title: 22sp
-   Body: 18sp
-   Caption: 16sp

Never use text smaller than 16sp.

------------------------------------------------------------------------

# Home Screen Layout

Top App Bar - Hamburger menu - Current month selector - Today shortcut

Main Area - Large weekday - Very large solar day number - No background
image - No wallpaper - Center aligned - Daily quote underneath

Three Information Cards 1. Good Hours 2. Lunar Date 3. Bad Hours

Activity Card - Things to do (max 3) - Things to avoid (max 3)

Solar Term Card - Current solar term - Next solar term - Date

Holiday/Event Card - Only visible when today's date has an event

Information Card - Can Chi - Hoàng đạo/Hắc đạo - 12 Trực - 28 Tú - Ngũ
hành - Daily score (0-100)

Bottom Navigation - Hôm nay - Lịch tháng - Ngày đẹp - Văn khấn - Thêm

------------------------------------------------------------------------

# Calendar Screen

-   Large month cells
-   Solar date
-   Small lunar date
-   Highlight Today
-   Highlight Full Moon
-   Highlight First Lunar Day
-   Holiday indicator
-   Tap to open Day Detail

------------------------------------------------------------------------

# Day Detail

Show: - Solar date - Lunar date - Can Chi - Good/Bad score - Hoàng
đạo/Hắc đạo - 12 Trực - 28 Tú - Good hours - Bad hours - Good
activities - Activities to avoid - Quote - Personal notes

------------------------------------------------------------------------

# Search Good Day

Support: - Wedding - Opening business - Travel - Construction - Contract
signing - House moving

Display: - Best dates - Score - Explanation

------------------------------------------------------------------------

# Accessibility

-   Android font scaling
-   High contrast
-   Touch target \>= 56dp
-   Icon + text only
-   One-hand friendly

------------------------------------------------------------------------

# Animation

-   Fade only
-   No bounce
-   No parallax
-   No unnecessary motion

------------------------------------------------------------------------

# Performance

-   Startup \< 1 second
-   Android 6+
-   Offline
-   No Firebase
-   No Analytics
-   No Ads

------------------------------------------------------------------------

# UI Principles

Priority: 1. Date 2. Lunar date 3. Good/Bad day 4. Good/Bad hours 5.
Activities 6. Solar term 7. Holiday 8. Quote 9. Extra information

If space is limited: - Remove less important information. - Never reduce
font size.

------------------------------------------------------------------------

# Overall Feeling

A premium digital Vietnamese desk calendar: - Simple enough for
grandparents - Modern enough for younger users - Elegant - Fast -
Peaceful - Timeless
