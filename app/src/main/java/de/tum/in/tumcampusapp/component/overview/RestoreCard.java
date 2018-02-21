package de.tum.in.tumcampusapp.component.overview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.overview.card.Card;
import de.tum.in.tumcampusapp.component.overview.card.StickyCard;

/**
 * Card that allows the user to reset the dismiss state of all cards
 */
public class RestoreCard extends StickyCard {

    public RestoreCard(Context context) {
        super(CardManager.CARD_RESTORE, context);
    }

    public static Card.CardViewHolder inflateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.card_restore, parent, false);
        return new Card.CardViewHolder(view);
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    /**
     * Override getPosition, we want the RestoreCard to be the last card.
     */
    @Override
    public int getPosition() {
        return Integer.MAX_VALUE;
    }
}
