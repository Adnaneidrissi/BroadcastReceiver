package com.example.tpn5exo2broadcastreceiver

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NetworkChangeReceiver(
    private val textView: TextView,
    private val context: Context
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo

        val isConnected = activeNetwork?.isConnected == true

        // Mettre à jour l'état de la connexion dans le TextView
        textView.text = if (isConnected) {
            "Connecté au réseau"
        } else {
            "Déconnecté du réseau"
        }

        // Envoyer une notification pour informer l'utilisateur de l'état du réseau
        sendNetworkStatusNotification(isConnected)
    }

    private fun sendNetworkStatusNotification(isConnected: Boolean) {
        // Vérifier si la permission de notification a été accordée (pour Android 13 et supérieur)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Si la permission n'est pas accordée, demandez-la
                ActivityCompat.requestPermissions(
                    (context as Activity),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1 // Vous pouvez choisir n'importe quel code de requête ici
                )
                return
            }
        }

        // Si la permission est accordée, envoyer la notification
        val notificationBuilder = NotificationCompat.Builder(context, "network_status_channel")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Statut du réseau")
            .setContentText(if (isConnected) "Vous êtes maintenant connecté au réseau." else "Vous êtes déconnecté du réseau.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, notificationBuilder.build())
    }

}