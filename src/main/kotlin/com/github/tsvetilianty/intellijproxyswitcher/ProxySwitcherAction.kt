package com.github.tsvetilianty.intellijproxyswitcher

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications.Bus.notifyAndHide
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.popup.PopupFactoryImpl
import com.intellij.util.net.HttpConfigurable
import javax.swing.Icon

class ProxySwitcherAction : AnAction("Proxy Switcher") {

    override fun actionPerformed(event: AnActionEvent) {
        val currentMode = getCurrentProxyMode()
        createSearchablePopup(listOf("Off", "Auto", "Manual"), getCurrentProxyMode()) { selectedOption ->
            if (currentMode == selectedOption) return@createSearchablePopup
            setProxyMode(selectedOption)
        }.showInFocusCenter()
    }

    private fun createSearchablePopup(
        options: List<String>,
        currentMode: String,
        onSelect: (String) -> Unit
    ): ListPopup {
        val step = object : BaseListPopupStep<String>("Select Proxy Mode", options) {

            override fun isSpeedSearchEnabled(): Boolean = true

            override fun getTextFor(value: String): String = value

            override fun getIconFor(value: String): Icon {
                return if (value == currentMode) AllIcons.Actions.Checked_selected else AllIcons.Actions.Close
            }

            override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                if (selectedValue != null) {
                    onSelect(selectedValue)
                }
                return PopupStep.FINAL_CHOICE
            }
        }

        return PopupFactoryImpl.getInstance().createListPopup(step)
    }

    private fun getCurrentProxyMode(): String {
        val httpConfigurable = HttpConfigurable.getInstance()
        return when {
            !httpConfigurable.USE_HTTP_PROXY && !httpConfigurable.USE_PROXY_PAC -> "Off"
            httpConfigurable.USE_PROXY_PAC -> "Auto"
            httpConfigurable.USE_HTTP_PROXY && !httpConfigurable.USE_PROXY_PAC -> "Manual"
            else -> "Off"
        }
    }

    private fun setProxyMode(option: String) {
        val httpConfigurable = HttpConfigurable.getInstance()
        when (option) {
            "Off" -> {
                httpConfigurable.USE_HTTP_PROXY = false
                httpConfigurable.USE_PROXY_PAC = false
            }

            "Auto" -> {
                httpConfigurable.USE_PROXY_PAC = true
                httpConfigurable.USE_HTTP_PROXY = false
            }

            "Manual" -> {
                httpConfigurable.USE_HTTP_PROXY = true
                httpConfigurable.USE_PROXY_PAC = false
            }
        }

        ApplicationManager.getApplication().saveSettings()
        showProxyModeNotification(option)
    }

    private fun showProxyModeNotification(proxyMode: String) {
        val notification = Notification(
            "ProxySwitcher",
            "Proxy mode",
            "Switched to: $proxyMode",
            NotificationType.INFORMATION
        )

        notifyAndHide(notification, null)
    }
}