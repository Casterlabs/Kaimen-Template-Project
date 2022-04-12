package co.casterlabs.kaimen.example;

import java.lang.reflect.InvocationTargetException;

import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.App.Appearance;
import co.casterlabs.kaimen.app.App.PowerManagementHint;
import co.casterlabs.kaimen.app.AppBootstrap;
import co.casterlabs.kaimen.app.AppEntry;
import co.casterlabs.kaimen.app.ui.UIServer;
import co.casterlabs.kaimen.util.platform.Platform;
import co.casterlabs.kaimen.webview.Webview;
import co.casterlabs.kaimen.webview.WebviewFactory;
import co.casterlabs.kaimen.webview.WebviewLifeCycleListener;
import co.casterlabs.kaimen.webview.WebviewWindowProperties;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Test {

    @Deprecated
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        // This is only here to facilitate the development of the app.
        // Kaimen will call another main method when launched.
        // Alternatively, you can configure your IDE to launch
        // co.casterlabs.kaimen.app.AppBootstrap and get rid of this method.
        AppBootstrap.main(args);
    }

    @AppEntry
    public static void entry() throws Exception {
        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG); // Show debug messages.

        // Setup the app
        App.setName("Example Project");
        App.setAppearance(Appearance.FOLLOW_SYSTEM);
        App.setPowermanagementHint(PowerManagementHint.BALANCED);

        // UI Server
        UIServer uiServer = new UIServer();

        uiServer.start();
        uiServer.setHandler((session) -> {
            // When we receive a request from the WebView, we send back our own HTML here.
            // You could also use this to communicate with the WV, but that's the IPC's job.
            return HttpResponse.newFixedLengthResponse(
                StandardHttpStatus.OK,
                "<!DOCTYPE html>"
                    + "<html style=\"background-color: transparent;\">"
                    + "<body style=\"text-align: center; font-family: BlinkMacSystemFont, -apple-system, 'Segoe UI', Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;\">"
                    + "<br />"
                    + "<br />"
                    + "Example App"
                    + "<br />"
                    + "<br />"
                    + "<a href='https://google.com'>Open Google</a>"
                    + "<p>x: <span id='x'></span> y: <span id='y'></span></p>"
                    + "<script>"
                    + "function onBridgeInit() {"
                    + "const xElem = document.querySelector('#x');"
                    + "const yElem = document.querySelector('#y');"
                    + "windowState.mutate('x', (x) => xElem.innerText = x);" // Watch for the position mutation, and update the display accordingly.
                    + "windowState.mutate('y', (y) => yElem.innerText = y);"
                    + "}"
                    + "</script>"
                    + "</body>"
                    + "</html"
            );
        });

        // Log some stuff
        FastLogger.logStatic("Running on: %s (%s)", Platform.os, Platform.arch);
        FastLogger.logStatic("Using: %s", WebviewFactory.get().getRendererType());
        FastLogger.logStatic("System Appearance: %s", App.getSystemAppearance());
        FastLogger.logStatic("UI Server port (it's ephemeral): %d", uiServer.getPort());

        // Setup the webview
        WebviewFactory factory = WebviewFactory.get();
        Webview webview = factory.produce();

        webview.initialize(new WebviewLifeCycleListener() {

            @SneakyThrows
            @Override
            public void onCloseRequested() {
                uiServer.close();
                System.exit(0);
            }

        }, null, false, false);

        webview.setProperties(
            new WebviewWindowProperties()
                .withAlwaysOnTop(true)
                .withFocusable(true)
        );

        webview.open(uiServer.getAddress());

        // And now, we setup the Bridge.
        // If you open inspect element (right click on the webview), goto console, and
        // type `await test.twelve` you'll get the value `12` back.
        // You can also set the value via `test.twelve = 13` and then `await
        // test.twelve` will return `13`.
        // We also create a sub object, `test.system`, which has `nanoTime()` which
        // return's Java's internal nanoseconds count and we also have a `testThrow()`
        // function which will throw an exception.
        // All Java exceptions (and stack traces) are passed back to Javascript (via
        // promise, so you'll need to `await test.system.testThrow()`) for ease of
        // debugging.

        webview
            .getBridge()
            .defineObject("test", new JavascriptObject() {
                @JavascriptValue
                private int twelve = 12;

                private JavascriptObject system = new JavascriptObject() {

                    @JavascriptFunction
                    public long nanoTime() {
                        return System.nanoTime();
                    }

                    @JavascriptFunction
                    public void testThrow() {
                        throw new IllegalStateException("Test throw.");
                    }

                };

            });

    }

}
