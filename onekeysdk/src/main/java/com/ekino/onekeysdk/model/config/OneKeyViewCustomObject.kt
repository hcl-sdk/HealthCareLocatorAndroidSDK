package com.ekino.onekeysdk.model.config

import android.graphics.Typeface
import com.ekino.onekeysdk.R
import com.ekino.onekeysdk.extensions.isNullable

/**
 * OneKeyViewCustomObject provides fields where the implementation app could be changed the style(s) like:
 * text color, text size, font(s), icon(s).
 *
 * Implementation:
 *
 * • With Java: OneKeyViewCustomObject obj = new OneKeyViewCustomObject.Builder().editIcon(`value`).build();
 *
 * • With Kotlin: OneKeyViewCustomObject.Builder(editIcon=`value`).build() or OneKeyViewCustomObject.Builder()..editIcon(`value`).build()
 *
 * If the implementation app doesn't provide those styles, the default will be applied.
 * @param colorPrimary Set primary color in the hex [String] color (Must start with #).
 * @param colorSecondary Set secondary color in the hex [String] color (Must start with #).
 * @param textColor Set text color in the hex [String] color (Must start with #).
 * @param colorMarker Set marker color in colorId which will show on the map (Must start with #).
 * @param colorMarkerSelected Set selected marker color in colorId which will show on the map (Must start with #).
 * @param fontDefault Set default font size in integer.
 * @param fontSmall Set font size for small level in [OneKeyViewFontObject].
 * @param searchIcon Set search icon in drawableId.
 * @param editIcon Set edit icon in drawableId.
 * @param markerIcon Set marker icon in drawableId.
 */
data class OneKeyViewCustomObject private constructor(
        val colorPrimary: String = "#43b02a",// Color in hex, must start with #
        val colorSecondary: String = "#00a3e0",
        val textColor: String = "#2d3c4d",
        val colorMarker: String = "#fe8a12",
        val colorMarkerSelected: String = "#fd8670",
        val fontButton: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "button", size = 14).build(),
        val fontDefault: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "default", size = 14).build(),
        val searchIcon: Int = R.drawable.baseline_search_white_24dp,
        val editIcon: Int = R.drawable.baseline_edit_white_36dp,
        val markerIcon: Int = R.drawable.baseline_location_on_white_36dp,
        val homeMode: Int = 0,
        var fontSearchInput: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "searchInput", size = 16).build(),
        val fontSmall: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "small", size = 12).build(),
        val fontTitleMain: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "titleMain", size = 20).build(),
        val fontTitleSecondary: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "titleSecondary", size = 16, weight = Typeface.BOLD).build(),
        val fontSearchResultTotal: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "searchResultTotal", size = 14, weight = Typeface.BOLD).build(),
        val fontSearchResultTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "searchResultTitle", size = 16).build(),
        val fontResultTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "resultTitle", size = 14).build(),
        val fontResultSubTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "resultSubTitle", size = 14).build(),
        val fontProfileTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "profileTitle", size = 18).build(),
        val fontProfileSubTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "profileSubTitle", size = 16).build(),
        val fontProfileTitleSection: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "profileTitleSection", size = 16).build(),
        val fontCardTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "cardTitle", size = 16, weight = Typeface.BOLD).build(),
        val fontModalTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "modalTitle", size = 18).build(),
        val fontSortCriteria: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "sortCriteria", size = 16).build(),
        val colorListBackground: String = "#f8f9fa", val colorDark: String, val colorGrey: String,
        val colorGreyDark: String, val colorGreyDarker: String, val colorGreyLight: String,
        val colorGreyLighter: String, val colorVoteUp: String, val colorVoteDown: String,
        val colorViewBackground: String, val colorCardBorder: String, val colorButtonBorder: String,
        val colorButtonBackground: String, val colorButtonAcceptBackground: String,
        val colorButtonDiscardBackground: String, val apiKey: String, val locale: String,
        val favoriteIds: ArrayList<String>) {

    @Suppress
    data class Builder(
            var colorPrimary: String = "#43b02a",// Color in hex, must start with #
            var colorSecondary: String = "#00a3e0",
            var textColor: String = "#2d3c4d",
            var colorMarker: String = "#fe8a12",
            var colorMarkerSelected: String = "#fd8670",
            var fontButton: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "button", size = 14).build(),
            var fontDefault: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "default", size = 14).build(),
            var searchIcon: Int = R.drawable.baseline_search_white_24dp,
            var editIcon: Int = R.drawable.baseline_edit_white_36dp,
            var markerIcon: Int = R.drawable.baseline_location_on_white_36dp,
            var homeMode: Int = 0,
            var fontSearchInput: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "searchInput", size = 16).build(),
            var fontSmall: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "small", size = 12).build(),

            var fontTitleMain: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "titleMain", size = 20).build(),
            var fontTitleSecondary: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "titleSecondary", size = 16, weight = Typeface.BOLD).build(),
            var fontSearchResultTotal: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "searchResultTotal", size = 14, weight = Typeface.BOLD).build(),
            var fontSearchResultTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "searchResultTitle", size = 16).build(),
            var fontResultTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "resultTitle", size = 14).build(),
            var fontResultSubTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "resultSubTitle", size = 14).build(),
            var fontProfileTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "profileTitle", size = 18).build(),
            var fontProfileSubTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "profileSubTitle", size = 16).build(),
            var fontProfileTitleSection: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "profileTitleSection", size = 16).build(),
            var fontCardTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "cardTitle", size = 16, weight = Typeface.BOLD).build(),
            var fontModalTitle: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "modalTitle", size = 18).build(),
            var fontSortCriteria: OneKeyViewFontObject = OneKeyViewFontObject.Builder(id = "sortCriteria", size = 16).build(),
            var colorListBackground: String = "#f8f9fa",
            var colorGreyLight: String = "#b8b8b8", var colorDark: String = "#2b3c4d",
            var colorGrey: String = "#a1a1a1",
            var colorGreyDark: String = "#7d7d7d",
            var colorGreyDarker: String = "#666666",
            var colorGreyLighter: String = "#ebebeb",
            var colorVoteDown: String = "#ff0000",
            var colorViewBackground: String = "#f8f9fa",
            var colorCardBorder: String = "#dedede",
            var colorButtonBorder: String = "#dedede",
            var colorButtonBackground: String = "#fcfcfc",
            var colorButtonDiscardBackground: String = "#9aa0a7",
            var apiKey: String = "1",
            var locale: String = "en",
            var favoriteIds: ArrayList<String> = arrayListOf()) {

        fun colorPrimary(primaryColor: String) = apply { this.colorPrimary = primaryColor }
        fun colorSecondary(secondaryColor: String) = apply { this.colorSecondary = secondaryColor }
        fun textColor(textColor: String) = apply { this.textColor = textColor }
        fun colorMarker(markerColor: String) = apply { this.colorMarker = markerColor }
        fun colorMarkerSelected(markerSelectedColor: String) = apply { this.colorMarkerSelected = markerSelectedColor }
        fun fontButton(fontButton: OneKeyViewFontObject) = apply { this.fontButton = fontButton }

        fun fontDefault(fontDefaultSize: OneKeyViewFontObject) = apply { this.fontDefault = fontDefaultSize }
        fun searchIcon(searchIcon: Int) = apply { this.searchIcon = searchIcon }
        fun editIcon(editIcon: Int) = apply { this.editIcon = editIcon }
        fun markerIcon(markerIcon: Int) = apply { this.markerIcon = markerIcon }
        fun homeMode(homeMode: Int) = apply { this.homeMode = homeMode }
        fun fontSearchInput(fontSearchInput: OneKeyViewFontObject?) = apply {
            if (fontSearchInput.isNullable()) return@apply
            this.fontSearchInput = fontSearchInput!!
        }

        fun fontSmall(fontSmallSize: OneKeyViewFontObject?) = apply {
            if (fontSmallSize.isNullable()) return@apply
            this.fontSmall = fontSmallSize!!
        }

        fun fontTitleMain(fontTitleMain: OneKeyViewFontObject?) = apply {
            if (fontTitleMain.isNullable()) return@apply
            this.fontTitleMain = fontTitleMain!!
        }

        fun fontTitleSecondary(fontTitleSecondary: OneKeyViewFontObject?) = apply {
            if (fontTitleSecondary.isNullable()) return@apply
            this.fontTitleSecondary = fontTitleSecondary!!
        }

        fun fontSearchResultTotal(fontSearchResultTotal: OneKeyViewFontObject?) = apply {
            this.fontSearchResultTotal = fontSearchResultTotal ?: this.fontSearchResultTotal
        }

        fun fontSearchResultTitle(fontSearchResultTitle: OneKeyViewFontObject?) = apply {
            if (fontSearchResultTitle.isNullable()) return@apply
            this.fontSearchResultTitle = fontSearchResultTitle!!
        }

        fun fontResultTitle(fontResultTitle: OneKeyViewFontObject?) = apply {
            this.fontResultTitle = fontResultTitle ?: this.fontResultTitle
        }

        fun fontResultSubTitle(fontResultSubTitle: OneKeyViewFontObject?) = apply {
            this.fontResultSubTitle = fontResultSubTitle ?: this.fontResultSubTitle
        }

        fun fontProfileTitle(fontProfileTitle: OneKeyViewFontObject?) = apply {
            this.fontProfileTitle = fontProfileTitle ?: this.fontProfileTitle
        }

        fun fontProfileSubTitle(fontProfileSubTitle: OneKeyViewFontObject?) = apply {
            this.fontProfileSubTitle = fontProfileSubTitle ?: this.fontProfileSubTitle
        }

        fun fontProfileTitleSection(fontProfileTitleSection: OneKeyViewFontObject?) = apply {
            this.fontProfileTitleSection = fontProfileTitleSection ?: this.fontProfileTitleSection
        }

        fun fontCardTitle(fontCardTitle: OneKeyViewFontObject?) = apply {
            this.fontCardTitle = fontCardTitle ?: this.fontCardTitle
        }

        fun fontModalTitle(fontModalTitle: OneKeyViewFontObject?) = apply {
            this.fontModalTitle = fontModalTitle ?: this.fontModalTitle
        }

        fun fontSortCriteria(fontSortCriteria: OneKeyViewFontObject?) = apply {
            this.fontSortCriteria = fontSortCriteria ?: this.fontSortCriteria
        }

        fun colorListBackground(colorListBackground: String) = apply { this.colorListBackground = colorListBackground }
        fun colorGreyLight(color: String) = apply { this.colorGreyLight = color }
        fun colorGrey(color: String) = apply { this.colorGrey = color }
        fun colorGreyDark(color: String) = apply { this.colorGreyDark = color }
        fun colorGreyDarker(color: String) = apply { this.colorGreyDarker = color }
        fun colorGreyLighter(color: String) = apply { this.colorGreyLighter = color }
        fun colorVoteDown(color: String) = apply { this.colorVoteDown = color }
        fun colorViewBackground(color: String) = apply { this.colorViewBackground = color }
        fun colorCardBorder(color: String) = apply { this.colorCardBorder = color }
        fun colorButtonBorder(color: String) = apply { this.colorButtonBorder = color }
        fun colorButtonBackground(color: String) = apply { this.colorButtonBackground = color }
        fun colorButtonDiscardBackground(color: String) = apply { this.colorButtonDiscardBackground = color }
        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }
        fun locale(locale: String) = apply { this.locale = locale }
        fun favoriteIds(favoriteIds: ArrayList<String>) = apply { this.favoriteIds = favoriteIds }

        fun build() = OneKeyViewCustomObject(colorPrimary, colorSecondary, textColor, colorMarker,
                colorMarkerSelected, fontButton, fontDefault, searchIcon, editIcon, markerIcon, homeMode,
                fontSearchInput, fontSmall, fontTitleMain, fontTitleSecondary, fontSearchResultTotal,
                fontSearchResultTitle, fontResultTitle, fontResultSubTitle, fontProfileTitle,
                fontProfileSubTitle, fontProfileTitleSection, fontCardTitle, fontModalTitle, fontSortCriteria,
                colorListBackground, colorDark, colorGrey, colorGreyDark, colorGreyDarker, colorGreyLight,
                colorGreyLighter, colorPrimary, colorVoteDown, colorViewBackground, colorCardBorder, colorButtonBorder,
                colorButtonBackground, colorPrimary, colorButtonDiscardBackground, apiKey, locale, favoriteIds)
    }
}