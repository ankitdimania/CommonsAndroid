package com.grootcode.android.ui;

import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SimpleSectionedListAdapter extends BaseAdapter {
    private boolean mValid = true;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private final int mSectionResourceId;

    /**
     * If the inflated resource is not a TextView, {@link #mFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private int mFieldId = 0;

    private final LayoutInflater mLayoutInflater;
    private final ListAdapter mBaseAdapter;
    private final SparseArray<Section> mSections = new SparseArray<Section>();

    public static class Section {
        int firstPosition;
        int sectionedPosition;
        CharSequence title;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public CharSequence getTitle() {
            return title;
        }
    }

    public SimpleSectionedListAdapter(Context context, int sectionResourceId, ListAdapter baseAdapter) {
        this(context, sectionResourceId, 0, baseAdapter);
    }

    public SimpleSectionedListAdapter(Context context, int sectionResourceId, int textViewResourceId,
            ListAdapter baseAdapter) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSectionResourceId = sectionResourceId;
        mFieldId = textViewResourceId;
        mBaseAdapter = baseAdapter;
        mBaseAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mValid = !mBaseAdapter.isEmpty();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                mValid = false;
                notifyDataSetInvalidated();
            }
        });
    }

    public void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, new Comparator<Section>() {
            @Override
            public int compare(Section o, Section o1) {
                return (o.firstPosition == o1.firstPosition)
                        ? 0
                        : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
            }
        });

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }

        notifyDataSetChanged();
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    public int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return ListView.INVALID_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }

    @Override
    public int getCount() {
        return (mValid ? mBaseAdapter.getCount() + mSections.size() : 0);
    }

    @Override
    public Object getItem(int position) {
        return isSectionHeaderPosition(position)
                ? mSections.get(position)
                : mBaseAdapter.getItem(sectionedPositionToPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? getViewTypeCount() - 1
                : mBaseAdapter.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableConditionalExpression
        return isSectionHeaderPosition(position)
                ? false
                : mBaseAdapter.isEnabled(sectionedPositionToPosition(position));
    }

    @Override
    public int getViewTypeCount() {
        return mBaseAdapter.getViewTypeCount() + 1; // the section headings
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return mBaseAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mBaseAdapter.isEmpty();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isSectionHeaderPosition(position)) {
            View view;
            TextView text;
            if (convertView == null) {
                view = mLayoutInflater.inflate(mSectionResourceId, parent, false);
            } else {
                view = convertView;
            }

            try {
                if (mFieldId == 0) {
                    // If no custom field is assigned, assume the whole resource is a TextView
                    text = (TextView) view;
                } else {
                    // Otherwise, find the TextView field within the layout
                    text = (TextView) view.findViewById(mFieldId);
                }
            } catch (ClassCastException e) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
            }

            text.setText(mSections.get(position).title);
            return view;

        } else {
            return mBaseAdapter.getView(sectionedPositionToPosition(position), convertView, parent);
        }
    }
}
