package local.tabhome;

import android.service.notification.NotificationListenerService;

/**
 * Пустая служба уведомлений. Сама по себе она нам не нужна - нужен только факт,
 * что она включена в "Доступ к уведомлениям": тогда MediaSessionManager разрешает
 * нашему приложению видеть, что сейчас играет в любом приложении, и управлять этим.
 */
public class NowPlayingListener extends NotificationListenerService {
}
