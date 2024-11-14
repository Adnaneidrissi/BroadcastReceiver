package com.example.tpn5exo2broadcastreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var connectionStatusTextView: TextView
    private lateinit var networkConnectivityReceiver: NetworkChangeReceiver
    private val CHANNEL_ID = "network_status_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialiser le TextView pour afficher l'état de la connexion
        connectionStatusTextView = findViewById(R.id.networkStatusTextView)

        // Initialiser et enregistrer le BroadcastReceiver
        networkConnectivityReceiver = NetworkChangeReceiver(connectionStatusTextView, this)

        // Enregistrer dynamiquement le BroadcastReceiver pour écouter les changements de connectivité réseau
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkConnectivityReceiver, filter)

        // Créer le canal de notification
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Désenregistrer le BroadcastReceiver pour éviter les fuites de mémoire
        unregisterReceiver(networkConnectivityReceiver)
    }

    // Créer un canal de notification (si nécessaire)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Network Status Channel"
            val descriptionText = "Notifications sur l'état de la connexion réseau"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Méthode pour envoyer la notification
    private fun sendNetworkStatusNotification(isConnected: Boolean) {
        // Vérifier si la permission de notification est accordée
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
                return
            }
        }

        // Construire la notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Statut du réseau")
            .setContentText(
                if (isConnected) "Vous êtes maintenant connecté au réseau."
                else "Vous êtes déconnecté du réseau."
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1, notificationBuilder.build())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Vérifier si la permission a été accordée
        if (requestCode == 1) { // Le même code de requête que dans requestPermissions
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, vous pouvez maintenant envoyer la notification
                sendNetworkStatusNotification(true) // Exemple de notification pour la connexion
            } else {
                // Permission refusée, gérer l'absence de la permission si nécessaire
                Toast.makeText(this, "La permission de notifications est requise pour envoyer une notification.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}