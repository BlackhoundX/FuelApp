package inft3970.fuelapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.Firebase;

/**
 * Class: FuelChartActivity
 * Author: Matt Couch
 * Purpose: This class displays the requested chart from Firebase Storage.
 * Creation Date: 30-Oct-17
 * Modification Date: 01-Nov-17
 */

public class FuelChartActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener{
    Context context;
    boolean spinnerTouched = false;  //Boolean to prevent the spinner from automatically selecting
    Button previousPrice;
    Button futurePrice;

    //Array containing all the postcodes for NSW
    public static String [] Postcode = new String[]{"Select One","1579","2000","2007","2008","2011","2015","2016","2017","2018","2019","2020","2021","2022","2025","2026","2027","2029",
            "2031","2032","2033","2034","2035","2036", "2038","2039","2040","2042","2043","2044","2045","2046","2047","2048","2049","2050","2056","2062","2064","2065","2066",
            "2067","2068","2069","2070","2071","2073", "2074","2075","2076","2077","2079","2081","2082","2085","2087","2088","2089","2090","2093","2095","2096","2099","2100",
            "2101","2103","2106","2107","2110","2111",
            "2112","2113","2114","2115","2116","2117","2118","2120","2121","2122","2125","2126","2127","2128","2130","2131","2132","2133","2134","2135","2136","2137","2138",
            "2140","2141","2142","2143","2144","2145","2146","2147","2148","2150","2151","2152","2153","2154","2155","2156","2158","2160","2161","2162","2163","2164","2165",
            "2166","2167","2168","2170","2171","2173","2175","2176","2177","2178","2179","2190","2191","2192","2193","2194","2195","2196","2197","2198","2199","2200","2203",
            "2204","2205","2206","2207","2208","2209","2210","2211","2212","2214","2216","2217","2218","2219","2220","2221","2222","2223","2224","2225","2226","2227","2228",
            "2229","2230","2232","2233","2234","2250","2251","2256","2257","2258","2259","2260","2261","2262","2263","2264","2265","2267","2280","2281","2282","2283","2284",
            "2285","2287","2289","2290","2291","2292","2293","2296","2298","2299","2300","2302","2303","2304","2305","2306","2307","2311","2312","2315","2316","2317","2318",
            "2319","2320","2321","2322","2323","2324","2325","2326","2327","2328","2329","2330","2333","2334","2336","2337","2338","2339","2340","2341","2342","2343","2345",
            "2346","2347","2350","2352","2353","2354","2355","2357","2358","2359","2360","2361","2365","2369","2370","2371","2372","2380","2382","2386","2388","2390","2396",
            "2397","2398","2400","2402","2403","2404","2405","2406","2409","2420","2421","2422","2423","2425","2426","2427","2428","2429","2430","2431","2439","2440","2441",
            "2443","2444","2445","2446","2447","2448","2450","2452","2453","2454","2455","2456","2460","2461","2462","2463","2464","2466","2469","2470","2471","2472","2473",
            "2474","2475","2476","2477","2478","2479","2480","2481","2482","2483","2484","2485","2486","2487","2489","2500","2502","2505","2506","2508","2515","2516","2517",
            "2518","2519","2525","2526","2527","2528","2529","2530","2533","2534","2535","2536","2537","2538","2539","2540","2541","2545","2546","2548","2549","2550","2551",
            "2557","2558","2560","2564","2565","2566","2567","2570","2571","2573","2574","2575","2576","2577","2578","2579","2580","2581","2582","2583","2586","2587","2590",
            "2594","2609","2619","2620","2621","2622","2627","2628","2629","2630","2632","2633","2640","2641","2642","2643","2644","2645","2646","2647","2648","2650","2652",
            "2653","2656","2658","2659","2660","2663","2665","2666","2668","2669","2671","2672","2675","2680","2681","2700","2701","2705","2706","2707","2710","2711","2712",
            "2713","2714","2715","2716","2717","2720","2721","2722","2726","2729","2731","2732","2733","2737","2745","2747","2749","2750","2751","2752","2753","2754","2756",
            "2757","2759","2760","2761","2763","2765","2766","2767","2768","2770","2773","2774","2775","2776","2777","2779","2780","2782","2783","2785","2786","2787","2790",
            "2792","2794","2795","2797","2799","2800","2804","2805","2806","2808","2810","2818","2820","2821","2823","2824","2825","2827","2828","2829","2830","2831","2832",
            "2833","2834","2835","2836","2839","2840","2843","2844","2845","2846","2847","2848","2849","2850","2852","2860","2865","2866","2867","2868","2869","2870","2871",
            "2873","2874","2875","2877","2878","2879","2880","2914","3644"};

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It initialises some variables and objects.
     * Returns: None
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_chart);
        Firebase.setAndroidContext(this);
        context = App.getContext();

        previousPrice = (Button)findViewById(R.id.previousPriceButton);
        futurePrice = (Button)findViewById(R.id.futurePriceButton);
    }

    /**
     * Method: onStart
     * Purpose: Automatically is called when the class is started. It creates, populates and activates the
     * spinner object.
     * Returns: None
     */
    @Override
    protected void onStart() {
        super.onStart();
        spinnerTouched = false;
        Spinner postCodeSpinner = (Spinner)findViewById(R.id.postcodeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,Postcode);

        postCodeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                spinnerTouched = true;
                return false;
            }
        });

        postCodeSpinner.setOnItemSelectedListener(this);
        postCodeSpinner.setAdapter(adapter);
    }

    /**
     * Method: onItemSelected
     * Purpose: The listener for the postcode spinner. It allows the user to select a postcode.
     * Returns: None
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(spinnerTouched) {
            String selectedPostcode = parent.getSelectedItem().toString();
            if(!selectedPostcode.equals("Select One")) {
                activateButtons(selectedPostcode);
            }
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * Method: activateButtons
     * Purpose: This method is called when a postcode is selected, and it activates the buttons
     * for displaying either future or past charts
     * Returns: None
     */
    public void activateButtons(final String postCode){
        previousPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedPostcode = postCode;
                Intent intent = new Intent(getBaseContext(), PostcodeChartDisplay.class);
                intent.putExtra("PostCodeValue", selectedPostcode);
                intent.putExtra("PastValues", true);
                Toast.makeText(getApplicationContext(), "Displaying past prices for " + selectedPostcode + ".", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });

        futurePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedPostcode = postCode;
                Intent intent = new Intent(getBaseContext(), PostcodeChartDisplay.class);
                intent.putExtra("PostCodeValue", selectedPostcode);
                intent.putExtra("PastValues", false);
                Toast.makeText(getApplicationContext(), "Displaying future price predictions for " + selectedPostcode + ".", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
    }
}
