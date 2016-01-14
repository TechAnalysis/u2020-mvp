package ru.ltst.u2020mvp.ui.debug;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.squareup.leakcanary.internal.DisplayLeakActivity;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.StatsSnapshot;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.mock.NetworkBehavior;
import ru.ltst.u2020mvp.BuildConfig;
import ru.ltst.u2020mvp.R;
import ru.ltst.u2020mvp.U2020App;
import ru.ltst.u2020mvp.data.AnimationSpeed;
import ru.ltst.u2020mvp.data.ApiEndpoint;
import ru.ltst.u2020mvp.data.ApiEndpoints;
import ru.ltst.u2020mvp.data.CaptureIntents;
import ru.ltst.u2020mvp.data.IsMockMode;
import ru.ltst.u2020mvp.data.LumberYard;
import ru.ltst.u2020mvp.data.NetworkDelay;
import ru.ltst.u2020mvp.data.NetworkFailurePercent;
import ru.ltst.u2020mvp.data.NetworkVariancePercent;
import ru.ltst.u2020mvp.data.PicassoDebugging;
import ru.ltst.u2020mvp.data.PixelGridEnabled;
import ru.ltst.u2020mvp.data.PixelRatioEnabled;
import ru.ltst.u2020mvp.data.ScalpelEnabled;
import ru.ltst.u2020mvp.data.ScalpelWireframeEnabled;
import ru.ltst.u2020mvp.data.api.mock.MockGalleryResponse;
import ru.ltst.u2020mvp.data.api.mock.MockGalleryService;
import ru.ltst.u2020mvp.data.prefs.InetSocketAddressPreferenceAdapter;
import ru.ltst.u2020mvp.ui.logs.LogsDialog;
import ru.ltst.u2020mvp.ui.misc.EnumAdapter;
import ru.ltst.u2020mvp.util.Keyboards;
import ru.ltst.u2020mvp.util.Strings;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class DebugView extends FrameLayout {
    private static final DateFormat DATE_DISPLAY_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
    @Bind(R.id.debug_contextual_title) View contextualTitleView;

    @Bind(R.id.debug_contextual_list) LinearLayout contextualListView;
    @Bind(R.id.debug_network_endpoint) Spinner endpointView;

    @Bind(R.id.debug_network_endpoint_edit) View endpointEditView;
    @Bind(R.id.debug_network_delay) Spinner networkDelayView;
    @Bind(R.id.debug_network_variance) Spinner networkVarianceView;
    @Bind(R.id.debug_network_error) Spinner networkErrorView;
    @Bind(R.id.debug_network_proxy) Spinner networkProxyView;
    @Bind(R.id.debug_network_logging) Spinner networkLoggingView;

    @Bind(R.id.debug_capture_intents) Switch captureIntentsView;
    @Bind(R.id.debug_repositories_response) Spinner repositoriesResponseView;

    @Bind(R.id.debug_ui_animation_speed) Spinner uiAnimationSpeedView;
    @Bind(R.id.debug_ui_pixel_grid) Switch uiPixelGridView;
    @Bind(R.id.debug_ui_pixel_ratio) Switch uiPixelRatioView;
    @Bind(R.id.debug_ui_scalpel) Switch uiScalpelView;
    @Bind(R.id.debug_ui_scalpel_wireframe) Switch uiScalpelWireframeView;
    @Bind(R.id.debug_build_name) TextView buildNameView;

    @Bind(R.id.debug_build_code) TextView buildCodeView;
    @Bind(R.id.debug_build_sha) TextView buildShaView;
    @Bind(R.id.debug_build_date) TextView buildDateView;
    @Bind(R.id.debug_device_make) TextView deviceMakeView;

    @Bind(R.id.debug_device_model) TextView deviceModelView;
    @Bind(R.id.debug_device_resolution) TextView deviceResolutionView;
    @Bind(R.id.debug_device_density) TextView deviceDensityView;
    @Bind(R.id.debug_device_release) TextView deviceReleaseView;
    @Bind(R.id.debug_device_api) TextView deviceApiView;
    @Bind(R.id.debug_picasso_indicators) Switch picassoIndicatorView;

    @Bind(R.id.debug_picasso_cache_size) TextView picassoCacheSizeView;
    @Bind(R.id.debug_picasso_cache_hit) TextView picassoCacheHitView;
    @Bind(R.id.debug_picasso_cache_miss) TextView picassoCacheMissView;
    @Bind(R.id.debug_picasso_decoded) TextView picassoDecodedView;
    @Bind(R.id.debug_picasso_decoded_total) TextView picassoDecodedTotalView;
    @Bind(R.id.debug_picasso_decoded_avg) TextView picassoDecodedAvgView;
    @Bind(R.id.debug_picasso_transformed) TextView picassoTransformedView;
    @Bind(R.id.debug_picasso_transformed_total) TextView picassoTransformedTotalView;
    @Bind(R.id.debug_picasso_transformed_avg) TextView picassoTransformedAvgView;
    @Bind(R.id.debug_okhttp_cache_max_size) TextView okHttpCacheMaxSizeView;

    @Bind(R.id.debug_okhttp_cache_write_error) TextView okHttpCacheWriteErrorView;
    @Bind(R.id.debug_okhttp_cache_request_count) TextView okHttpCacheRequestCountView;
    @Bind(R.id.debug_okhttp_cache_network_count) TextView okHttpCacheNetworkCountView;
    @Bind(R.id.debug_okhttp_cache_hit_count) TextView okHttpCacheHitCountView;

    @Inject OkHttpClient client;
    @Inject @Named("Api") OkHttpClient apiClient;
    @Inject Picasso picasso;
    @Inject LumberYard lumberYard;
    @Inject @IsMockMode boolean isMockMode;
    @Inject @ApiEndpoint Preference<String> networkEndpoint;
    @Inject Preference<InetSocketAddress> networkProxyAddress;
    @Inject @CaptureIntents Preference<Boolean> captureIntents;
    @Inject @AnimationSpeed Preference<Integer> animationSpeed;
    @Inject @PicassoDebugging Preference<Boolean> picassoDebugging;
    @Inject @PixelGridEnabled Preference<Boolean> pixelGridEnabled;
    @Inject @PixelRatioEnabled Preference<Boolean> pixelRatioEnabled;
    @Inject @ScalpelEnabled Preference<Boolean> scalpelEnabled;
    @Inject @ScalpelWireframeEnabled Preference<Boolean> scalpelWireframeEnabled;
    @Inject NetworkBehavior behavior;
    @Inject @NetworkDelay Preference<Long> networkDelay;
    @Inject @NetworkFailurePercent Preference<Integer> networkFailurePercent;
    @Inject @NetworkVariancePercent Preference<Integer> networkVariancePercent;
    @Inject MockGalleryService mockGalleryService;
    @Inject Application app;

    private final ContextualDebugActions contextualDebugActions;

    public DebugView(Context context) {
        this(context, null);
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        U2020App.get(context).component().inject(this);

        // Inflate all of the controls and inject them.
        LayoutInflater.from(context).inflate(R.layout.debug_view_content, this);
        ButterKnife.bind(this);

        Set<ContextualDebugActions.DebugAction<?>> debugActions = Collections.emptySet();
        contextualDebugActions = new ContextualDebugActions(this, debugActions);

        setupNetworkSection();
        setupMockBehaviorSection();
        setupUserInterfaceSection();
        setupBuildSection();
        setupDeviceSection();
        setupPicassoSection();
        setupOkHttpCacheSection();
    }

    public ContextualDebugActions getContextualDebugActions() {
        return contextualDebugActions;
    }

    public void onDrawerOpened() {
        refreshPicassoStats();
        refreshOkHttpCacheStats();
    }

    private void setupNetworkSection() {
        final ApiEndpoints currentEndpoint = ApiEndpoints.from(networkEndpoint.get());
        final EnumAdapter<ApiEndpoints> endpointAdapter =
                new EnumAdapter<>(getContext(), ApiEndpoints.class);
        endpointView.setAdapter(endpointAdapter);
        endpointView.setSelection(currentEndpoint.ordinal());
        endpointView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                ApiEndpoints selected = endpointAdapter.getItem(position);
                if (selected != currentEndpoint) {
                    if (selected == ApiEndpoints.CUSTOM) {
                        Timber.d("Custom network endpoint selected. Prompting for URL.");
                        showCustomEndpointDialog(currentEndpoint.ordinal(), "http://");
                    } else {
                        setEndpointAndRelaunch(selected.url);
                    }
                } else {
                    Timber.d("Ignoring re-selection of network endpoint %s", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final NetworkDelayAdapter delayAdapter = new NetworkDelayAdapter(getContext());
        networkDelayView.setAdapter(delayAdapter);
        networkDelayView.setSelection(
                NetworkDelayAdapter.getPositionForValue(behavior.delay(MILLISECONDS)));
        networkDelayView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                long selected = delayAdapter.getItem(position);
                if (selected != behavior.delay(MILLISECONDS)) {
                    Timber.d("Setting network delay to %sms", selected);
                    behavior.setDelay(selected, MILLISECONDS);
                    networkDelay.set(selected);
                } else {
                    Timber.d("Ignoring re-selection of network delay %sms", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final NetworkVarianceAdapter varianceAdapter = new NetworkVarianceAdapter(getContext());
        networkVarianceView.setAdapter(varianceAdapter);
        networkVarianceView.setSelection(
                NetworkVarianceAdapter.getPositionForValue(behavior.variancePercent()));
        networkVarianceView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selected = varianceAdapter.getItem(position);
                if (selected != behavior.variancePercent()) {
                    Timber.d("Setting network variance to %s%%", selected);
                    behavior.setVariancePercent(selected);
                    networkVariancePercent.set(selected);
                } else {
                    Timber.d("Ignoring re-selection of network variance %s%%", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final NetworkErrorAdapter errorAdapter = new NetworkErrorAdapter(getContext());
        networkErrorView.setAdapter(errorAdapter);
        networkErrorView.setSelection(
                NetworkErrorAdapter.getPositionForValue(behavior.failurePercent()));
        networkErrorView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selected = errorAdapter.getItem(position);
                if (selected != behavior.failurePercent()) {
                    Timber.d("Setting network error to %s%%", selected);
                    behavior.setFailurePercent(selected);
                    networkFailurePercent.set(selected);
                } else {
                    Timber.d("Ignoring re-selection of network error %s%%", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        int currentProxyPosition = networkProxyAddress.isSet() ? ProxyAdapter.PROXY : ProxyAdapter.NONE;
        final ProxyAdapter proxyAdapter = new ProxyAdapter(getContext(), networkProxyAddress);
        networkProxyView.setAdapter(proxyAdapter);
        networkProxyView.setSelection(currentProxyPosition);

        networkProxyView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == ProxyAdapter.NONE) {
                    Timber.d("Clearing network proxy");
                    // TODO: Keep the custom proxy around so you can easily switch back and forth.
                    networkProxyAddress.delete();
                    client.setProxy(null);
                    apiClient.setProxy(null);
                } else if (networkProxyAddress.isSet() && position == ProxyAdapter.PROXY) {
                    Timber.d("Ignoring re-selection of network proxy %s", networkProxyAddress.get());
                } else {
                    Timber.d("New network proxy selected. Prompting for host.");
                    showNewNetworkProxyDialog(proxyAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Only show the endpoint editor when a custom endpoint is in use.
        endpointEditView.setVisibility(currentEndpoint == ApiEndpoints.CUSTOM ? VISIBLE : GONE);

        if (currentEndpoint == ApiEndpoints.MOCK_MODE) {
            // Disable network proxy if we are in mock mode.
            networkProxyView.setEnabled(false);
            networkLoggingView.setEnabled(false);
        } else {
            // Disable network controls if we are not in mock mode.
            networkDelayView.setEnabled(false);
            networkVarianceView.setEnabled(false);
            networkErrorView.setEnabled(false);
        }

        // We use the JSON rest adapter as the source of truth for the log level.
//        final EnumAdapter<RestAdapter.LogLevel> loggingAdapter =
//                new EnumAdapter<>(getContext(), RestAdapter.LogLevel.class);
//        networkLoggingView.setAdapter(loggingAdapter);
//        networkLoggingView.setSelection(restAdapter.getLogLevel().ordinal());
//        networkLoggingView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
//                RestAdapter.LogLevel selected = loggingAdapter.getItem(position);
//                if (selected != restAdapter.getLogLevel()) {
//                    Timber.d("Setting logging level to %s", selected);
//                    restAdapter.setLogLevel(selected);
//                } else {
//                    Timber.d("Ignoring re-selection of logging level " + selected);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });
    }

    @OnClick(R.id.debug_network_endpoint_edit)
    void onEditEndpointClicked() {
        Timber.d("Prompting to edit custom endpoint URL.");
        // Pass in the currently selected position since we are merely editing.
        showCustomEndpointDialog(endpointView.getSelectedItemPosition(), networkEndpoint.get());
    }

    private void setupMockBehaviorSection() {
        captureIntentsView.setEnabled(isMockMode);
        captureIntentsView.setChecked(captureIntents.get());
        captureIntentsView.setOnCheckedChangeListener((compoundButton, b) -> {
            Timber.d("Capture intents set to %s", b);
            captureIntents.set(b);
        });

        configureResponseSpinner(repositoriesResponseView, MockGalleryResponse.class);
    }

    /**
     * Populates a {@code Spinner} with the values of an {@code enum} and binds it to the value set
     * in
     * the mock service.
     */
    private <T extends Enum<T>> void configureResponseSpinner(Spinner spinner,
                                                              final Class<T> responseClass) {
        final EnumAdapter<T> adapter = new EnumAdapter<>(getContext(), responseClass);
        spinner.setEnabled(isMockMode);
        spinner.setAdapter(adapter);
        spinner.setSelection(mockGalleryService.getResponse(responseClass).ordinal());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                T selected = adapter.getItem(position);
                if (selected != mockGalleryService.getResponse(responseClass)) {
                    Timber.d("Setting %s to %s", responseClass.getSimpleName(), selected);
                    mockGalleryService.setResponse(responseClass, selected);
                    ProcessPhoenix.triggerRebirth(getContext());
                } else {
                    Timber.d("Ignoring re-selection of %s %s", responseClass.getSimpleName(), selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupUserInterfaceSection() {
        final AnimationSpeedAdapter speedAdapter = new AnimationSpeedAdapter(getContext());
        uiAnimationSpeedView.setAdapter(speedAdapter);
        final int animationSpeedValue = animationSpeed.get();
        uiAnimationSpeedView.setSelection(
                AnimationSpeedAdapter.getPositionForValue(animationSpeedValue));
        uiAnimationSpeedView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int selected = speedAdapter.getItem(position);
                if (selected != animationSpeed.get()) {
                    Timber.d("Setting animation speed to %sx", selected);
                    animationSpeed.set(selected);
                    applyAnimationSpeed(selected);
                } else {
                    Timber.d("Ignoring re-selection of animation speed %sx", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // Ensure the animation speed value is always applied across app restarts.
        post(() -> applyAnimationSpeed(animationSpeedValue));

        boolean gridEnabled = pixelGridEnabled.get();
        uiPixelGridView.setChecked(gridEnabled);
        uiPixelRatioView.setEnabled(gridEnabled);
        uiPixelGridView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Timber.d("Setting pixel grid overlay enabled to %b", isChecked);
            pixelGridEnabled.set(isChecked);
            uiPixelRatioView.setEnabled(isChecked);
        });

        uiPixelRatioView.setChecked(pixelRatioEnabled.get());
        uiPixelRatioView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Timber.d("Setting pixel scale overlay enabled to %b", isChecked);
            pixelRatioEnabled.set(isChecked);
        });

        uiScalpelView.setChecked(scalpelEnabled.get());
        uiScalpelWireframeView.setEnabled(scalpelEnabled.get());
        uiScalpelView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Timber.d("Setting scalpel interaction enabled to %b", isChecked);
            scalpelEnabled.set(isChecked);
            uiScalpelWireframeView.setEnabled(isChecked);
        });

        uiScalpelWireframeView.setChecked(scalpelWireframeEnabled.get());
        uiScalpelWireframeView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Timber.d("Setting scalpel wireframe enabled to %b", isChecked);
            scalpelWireframeEnabled.set(isChecked);
        });
    }

    @OnClick(R.id.debug_logs_show)
    void showLogs() {
        new LogsDialog(new ContextThemeWrapper(getContext(), R.style.Theme_U2020), lumberYard).show();
    }

    @OnClick(R.id.debug_leaks_show)
    void showLeaks() {
        Intent intent = new Intent(getContext(), DisplayLeakActivity.class);
        getContext().startActivity(intent);
    }

    private void setupBuildSection() {
        buildNameView.setText(BuildConfig.VERSION_NAME);
        buildCodeView.setText(String.valueOf(BuildConfig.VERSION_CODE));
        buildShaView.setText(BuildConfig.GIT_SHA);

        try {
            // Parse ISO8601-format time into local time.
            DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
            inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date buildTime = inFormat.parse(BuildConfig.BUILD_TIME);
            buildDateView.setText(DATE_DISPLAY_FORMAT.format(buildTime));
        } catch (ParseException e) {
            throw new RuntimeException("Unable to decode build time: " + BuildConfig.BUILD_TIME, e);
        }
    }

    private void setupDeviceSection() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        String densityBucket = getDensityString(displayMetrics);
        deviceMakeView.setText(Strings.truncateAt(Build.MANUFACTURER, 20));
        deviceModelView.setText(Strings.truncateAt(Build.MODEL, 20));
        deviceResolutionView.setText(displayMetrics.heightPixels + "x" + displayMetrics.widthPixels);
        deviceDensityView.setText(displayMetrics.densityDpi + "dpi (" + densityBucket + ")");
        deviceReleaseView.setText(Build.VERSION.RELEASE);
        deviceApiView.setText(String.valueOf(Build.VERSION.SDK_INT));
    }

    private void setupPicassoSection() {
        boolean picassoDebuggingValue = picassoDebugging.get();
        picasso.setIndicatorsEnabled(picassoDebuggingValue);
        picassoIndicatorView.setChecked(picassoDebuggingValue);
        picassoIndicatorView.setOnCheckedChangeListener((button, isChecked) -> {
          Timber.d("Setting Picasso debugging to %b", isChecked);
          picasso.setIndicatorsEnabled(isChecked);
          picassoDebugging.set(isChecked);
        });

        refreshPicassoStats();
    }

    private void refreshPicassoStats() {
        StatsSnapshot snapshot = picasso.getSnapshot();
        String size = getSizeString(snapshot.size);
        String total = getSizeString(snapshot.maxSize);
        int percentage = (int) ((1f * snapshot.size / snapshot.maxSize) * 100);
        picassoCacheSizeView.setText(size + " / " + total + " (" + percentage + "%)");
        picassoCacheHitView.setText(String.valueOf(snapshot.cacheHits));
        picassoCacheMissView.setText(String.valueOf(snapshot.cacheMisses));
        picassoDecodedView.setText(String.valueOf(snapshot.originalBitmapCount));
        picassoDecodedTotalView.setText(getSizeString(snapshot.totalOriginalBitmapSize));
        picassoDecodedAvgView.setText(getSizeString(snapshot.averageOriginalBitmapSize));
        picassoTransformedView.setText(String.valueOf(snapshot.transformedBitmapCount));
        picassoTransformedTotalView.setText(getSizeString(snapshot.totalTransformedBitmapSize));
        picassoTransformedAvgView.setText(getSizeString(snapshot.averageTransformedBitmapSize));
    }

    private void setupOkHttpCacheSection() {
        Cache cache = client.getCache(); // Shares the cache with apiClient, so no need to check both.
        okHttpCacheMaxSizeView.setText(getSizeString(cache.getMaxSize()));

        refreshOkHttpCacheStats();
    }

    private void refreshOkHttpCacheStats() {
        Cache cache = client.getCache(); // Shares the cache with apiClient, so no need to check both.
        int writeTotal = cache.getWriteSuccessCount() + cache.getWriteAbortCount();
        int percentage = (int) ((1f * cache.getWriteAbortCount() / writeTotal) * 100);
        okHttpCacheWriteErrorView.setText(
                cache.getWriteAbortCount() + " / " + writeTotal + " (" + percentage + "%)");
        okHttpCacheRequestCountView.setText(String.valueOf(cache.getRequestCount()));
        okHttpCacheNetworkCountView.setText(String.valueOf(cache.getNetworkCount()));
        okHttpCacheHitCountView.setText(String.valueOf(cache.getHitCount()));
    }

    private void applyAnimationSpeed(int multiplier) {
        try {
            Method method = ValueAnimator.class.getDeclaredMethod("setDurationScale", float.class);
            method.invoke(null, (float) multiplier);
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply animation speed.", e);
        }
    }

    private static String getDensityString(DisplayMetrics displayMetrics) {
        switch (displayMetrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxxhdpi";
            case DisplayMetrics.DENSITY_TV:
                return "tvdpi";
            default:
                return String.valueOf(displayMetrics.densityDpi);
        }
    }

    private static String getSizeString(long bytes) {
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int unit = 0;
        while (bytes >= 1024) {
            bytes /= 1024;
            unit += 1;
        }
        return bytes + units[unit];
    }

    private void showNewNetworkProxyDialog(final ProxyAdapter proxyAdapter) {
        final int originalSelection = networkProxyAddress.isSet() ? ProxyAdapter.PROXY : ProxyAdapter.NONE;

        View view = LayoutInflater.from(app).inflate(R.layout.debug_drawer_network_proxy, null);
        final EditText hostView = findById(view, R.id.debug_drawer_network_proxy_host);

        if(networkProxyAddress.isSet()) {
            String host = networkProxyAddress.get().getHostName();
            hostView.setText(host); // Set the current host.
            hostView.setSelection(0, host.length()); // Pre-select it for editing.

            // Show the keyboard. Post this to the next frame when the dialog has been attached.
            hostView.post(() -> Keyboards.showKeyboard(hostView));
        }

        new android.support.v7.app.AlertDialog.Builder(getContext()) //
                .setTitle("Set Network Proxy")
                .setView(view)
                .setNegativeButton("Cancel", (dialog, i) -> {
                    networkProxyView.setSelection(originalSelection);
                    dialog.cancel();
                })
                .setPositiveButton("Use", (dialog, i) -> {
                    String in = hostView.getText().toString();
                    InetSocketAddress address = InetSocketAddressPreferenceAdapter.parse(in);
                    if (address != null) {
                        networkProxyAddress.set(address); // Persist across restarts.
                        proxyAdapter.notifyDataSetChanged(); // Tell the spinner to update.
                        networkProxyView.setSelection(ProxyAdapter.PROXY); // And show the proxy.

                        Proxy proxy = InetSocketAddressPreferenceAdapter.createProxy(address);
                        client.setProxy(proxy);
                        apiClient.setProxy(proxy);
                    } else {
                        networkProxyView.setSelection(originalSelection);
                    }
                })
                .setOnCancelListener(dialogInterface -> networkProxyView.setSelection(originalSelection))
                .show();
    }

    private void showCustomEndpointDialog(final int originalSelection, String defaultUrl) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.debug_drawer_network_endpoint, null);
        final EditText url = findById(view, R.id.debug_drawer_network_endpoint_url);
        url.setText(defaultUrl);
        url.setSelection(url.length());

        new AlertDialog.Builder(getContext()) //
            .setTitle("Set Network Endpoint")
            .setView(view)
            .setNegativeButton("Cancel", (dialog, i) -> {
                endpointView.setSelection(originalSelection);
                dialog.cancel();
            })
            .setPositiveButton("Use", (dialog, i) -> {
                String theUrl = url.getText().toString();
                if (!Strings.isBlank(theUrl)) {
                    setEndpointAndRelaunch(theUrl);
                } else {
                    endpointView.setSelection(originalSelection);
                }
            })
            .setOnCancelListener(dialogInterface -> endpointView.setSelection(originalSelection))
            .show();
    }

    private void setEndpointAndRelaunch(String endpoint) {
        Timber.d("Setting network endpoint to %s", endpoint);
        networkEndpoint.set(endpoint);

        ProcessPhoenix.triggerRebirth(getContext());
    }
}
