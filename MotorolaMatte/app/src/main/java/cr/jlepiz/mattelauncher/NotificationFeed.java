package cr.jlepiz.mattelauncher;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Solo procesa avisos visibles de Teams, Outlook y WhatsApp/WA Business.
 * Los mensajes se guardan ÚNICAMENTE en memoria, nunca en disco ni en la red.
 */
public final class NotificationFeed extends NotificationListenerService {
    static final class Preview {
        final String group, title, message;
        final long when;
        final PendingIntent action;
        Preview(String group,String title,String message,long when,PendingIntent action) {
            this.group=group;this.title=title;this.message=message;this.when=when;this.action=action;
        }
    }
    private static final List<Preview> recent=new ArrayList<>();
    private static WeakReference<MainActivity> main=new WeakReference<>(null);
    static synchronized void attach(MainActivity activity) { main=new WeakReference<>(activity); }
    static synchronized void detach(MainActivity activity) {
        if (main.get()==activity) main.clear();
    }
    static synchronized Preview latest(String group) {
        for (Preview p: recent) if (p.group.equals(group)) return p;
        return null;
    }
    static boolean accessGranted(Context c) {
        String enabled=Settings.Secure.getString(c.getContentResolver(),"enabled_notification_listeners");
        if (enabled==null) return false;
        String component=c.getPackageName()+"/"+NotificationFeed.class.getName();
        return enabled.contains(component) || enabled.contains(c.getPackageName()+"/.NotificationFeed");
    }
    private static String group(String pkg) {
        if ("com.whatsapp".equals(pkg)||"com.whatsapp.w4b".equals(pkg)) return "whatsapp";
        if ("com.microsoft.teams".equals(pkg)||"com.microsoft.teams2".equals(pkg)) return "teams";
        if ("com.microsoft.office.outlook".equals(pkg)) return "outlook";
        return null;
    }
    private static String plain(CharSequence str) {
        if (str==null) return "";
        String value=str.toString().replaceAll("\\s+"," ").trim();
        if (value.length()>160) value=value.substring(0,160)+"…";
        return value;
    }
    private void rebuild() {
        ArrayList<Preview> fresh=new ArrayList<>();
        try {
            StatusBarNotification[] active=getActiveNotifications();
            if (active!=null) for(StatusBarNotification sbn:active) {
                String source=group(sbn.getPackageName());
                if (source==null) continue;
                Notification note=sbn.getNotification();
                if (note==null || (note.flags & Notification.FLAG_GROUP_SUMMARY)!=0) continue;
                String title=plain(note.extras.getCharSequence(Notification.EXTRA_TITLE));
                String content=plain(note.extras.getCharSequence(Notification.EXTRA_TEXT));
                if (content.isEmpty()) content=plain(note.extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
                if (content.isEmpty() && title.isEmpty()) continue;
                fresh.add(new Preview(source,title,content,sbn.getPostTime(),note.contentIntent));
            }
        } catch (SecurityException ignored) {
            // Si Android revoca el permiso, dejamos la lista vacía.
        }
        fresh.sort((a,b)->Long.compare(b.when,a.when));
        MainActivity target;
        synchronized (NotificationFeed.class) {
            recent.clear();
            for (Preview p:fresh) {
                boolean seen=false;
                for (Preview old:recent) if (old.group.equals(p.group)) { seen=true; break; }
                if (!seen) recent.add(p);
            }
            target=main.get();
        }
        if (target!=null) target.runOnUiThread(target::refreshPreviews);
    }
    @Override public void onListenerConnected() { rebuild(); }
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (group(sbn.getPackageName())!=null) rebuild();
    }
    @Override public void onNotificationRemoved(StatusBarNotification sbn) {
        if (group(sbn.getPackageName())!=null) rebuild();
    }
    @Override public void onListenerDisconnected() {
        MainActivity target;
        synchronized(NotificationFeed.class) { recent.clear();target=main.get(); }
        if (target!=null) target.runOnUiThread(target::refreshPreviews);
    }
}
