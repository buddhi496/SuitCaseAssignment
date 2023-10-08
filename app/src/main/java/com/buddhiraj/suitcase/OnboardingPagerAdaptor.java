package com.buddhiraj.suitcase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class OnboardingPagerAdaptor extends PagerAdapter {
    private int[] images;
    private String[] texts;
    private String[] messages;
    private LayoutInflater layoutInflater;
    private Context context;

    public OnboardingPagerAdaptor(Context context, int[] images, String[] texts, String[] messages) {
        this.context = context;
        this.images = images;
        this.texts = texts;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding, container, false);

        ImageView imageView = view.findViewById(R.id.imageViewOB);
        TextView textView = view.findViewById(R.id.textViewOB);
        TextView messageView = view.findViewById(R.id.message);

        imageView.setImageResource(images[position]);
        textView.setText(texts[position]);
        messageView.setText(messages[position]);


        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

