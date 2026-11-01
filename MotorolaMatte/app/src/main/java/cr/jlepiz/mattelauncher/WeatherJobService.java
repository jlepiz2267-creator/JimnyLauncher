package cr.jlepiz.mattelauncher;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobInfo;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/** Android programa estas actualizaciones de forma aproximada, no a intervalos exactos. */
public final class WeatherJobService extends JobService {
    private static final int JOB_ID=6207;
    static void schedule(Context ctx) {
        JobScheduler scheduler=(JobScheduler)ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler==null) return;
        if (!WeatherData.alerts(ctx) || !WeatherData.hasCoords(ctx)) {
            scheduler.cancel(JOB_ID); return;
        }
        JobInfo info=new JobInfo.Builder(JOB_ID,new ComponentName(ctx,WeatherJobService.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(2L*60*60*1000,30L*60*1000)
            .build();
        scheduler.schedule(info);
    }
    @Override public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            try { if (WeatherData.alerts(this) && WeatherData.hasCoords(this))
                WeatherData.fetch(this,true);
            } catch (Exception ignored) {
                // Si no hay conexión, el clima anterior permanece en la pantalla.
            } finally { jobFinished(params,false); }
        },"matte-clima-job").start();
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) { return true; }
}
