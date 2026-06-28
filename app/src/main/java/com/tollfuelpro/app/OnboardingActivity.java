package com.tollfuelpro.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;
import com.tollfuelpro.app.adapters.OnboardingAdapter;
import com.tollfuelpro.app.models.OnboardingItem;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private Button buttonOnboardingAction;
    private Button buttonSkip;
    private ViewPager2 onboardingViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        layoutOnboardingIndicators = findViewById(R.id.indicators);
        buttonOnboardingAction = findViewById(R.id.btnNext);
        buttonSkip = findViewById(R.id.btnSkip);
        onboardingViewPager = findViewById(R.id.viewPager);

        setupOnboardingItems();

        onboardingViewPager.setAdapter(onboardingAdapter);
        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
                if (position == onboardingAdapter.getItemCount() - 1) {
                    buttonOnboardingAction.setText(getString(R.string.get_started));
                    buttonSkip.setVisibility(View.INVISIBLE);
                } else {
                    buttonOnboardingAction.setText(getString(R.string.next));
                    buttonSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonOnboardingAction.setOnClickListener(v -> {
            if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        buttonSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        OnboardingItem itemCalculator = new OnboardingItem();
        itemCalculator.setTitle(getString(R.string.onboarding_title_1));
        itemCalculator.setDescription(getString(R.string.onboarding_desc_1));
        itemCalculator.setImage(R.drawable.ic_calculate_new);

        OnboardingItem itemHistory = new OnboardingItem();
        itemHistory.setTitle(getString(R.string.onboarding_title_2));
        itemHistory.setDescription(getString(R.string.onboarding_desc_2));
        itemHistory.setImage(R.drawable.ic_history);

        OnboardingItem itemShare = new OnboardingItem();
        itemShare.setTitle(getString(R.string.onboarding_title_3));
        itemShare.setDescription(getString(R.string.onboarding_desc_3));
        itemShare.setImage(R.drawable.ic_share);

        onboardingItems.add(itemCalculator);
        onboardingItems.add(itemHistory);
        onboardingItems.add(itemShare);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.ic_dot
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        int childCount = layoutOnboardingIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if (i == index) {
                // Active dot
                imageView.setImageTintList(ContextCompat.getColorStateList(this, R.color.accent));
            } else {
                // Inactive dot
                imageView.setImageTintList(ContextCompat.getColorStateList(this, R.color.text_secondary));
            }
        }
    }

    private void finishOnboarding() {
        SharedPreferences sharedPreferences = getSharedPreferences("TollFuelProPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstTimeSetup", false);
        editor.apply();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
