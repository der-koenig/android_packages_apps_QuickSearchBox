/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.quicksearchbox;

import com.android.quicksearchbox.util.NamedTaskExecutor;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Creates corpora.
 */
public class SearchableCorpusFactory implements CorpusFactory {

    private final Context mContext;

    private final NamedTaskExecutor mExecutor;

    public SearchableCorpusFactory(Context context, NamedTaskExecutor executor) {
        mContext = context;
        mExecutor = executor;
    }

    public Collection<Corpus> createCorpora(Sources sources) {
        ArrayList<Corpus> corpora = new ArrayList<Corpus>();
        addSpecialCorpora(corpora, sources);
        addSingleSourceCorpora(corpora, sources);
        return corpora;
    }

    protected Context getContext() {
        return mContext;
    }

    protected NamedTaskExecutor getExecutor() {
        return mExecutor;
    }

    /**
     * Adds any corpora that are not simple single source corpora.
     *
     * @param corpora List to add corpora to.
     * @param sources All available sources.
     */
    protected void addSpecialCorpora(ArrayList<Corpus> corpora, Sources sources) {
        corpora.add(createWebCorpus(sources));
        corpora.add(createAppsCorpus(sources));
    }

    /**
     * Adds corpora for all sources that are not already used by a corpus.
     *
     * @param corpora List to add the new corpora to. Corpora will not be created for the sources
     *        used by corpora already in this list.
     * @param sources Sources to create corpora for.
     */
    protected void addSingleSourceCorpora(ArrayList<Corpus> corpora, Sources sources) {
        // Set of all sources that are already used
        HashSet<Source> claimedSources = new HashSet<Source>();
        for (Corpus specialCorpus : corpora) {
            claimedSources.addAll(specialCorpus.getSources());
        }

        // Creates corpora for all unclaimed sources
        for (Source source : sources.getSources()) {
            if (!claimedSources.contains(source)) {
                corpora.add(createSingleSourceCorpus(source));
            }
        }
    }

    protected Corpus createWebCorpus(Sources sources) {
        Source webSource = sources.getWebSearchSource();
        Source browserSource = getBrowserSource(sources);
        return new WebCorpus(mContext, mExecutor, webSource, browserSource);
    }

    protected Corpus createAppsCorpus(Sources sources) {
        Source appsSource = getAppsSource(sources);
        return new AppsCorpus(mContext, mExecutor, appsSource);
    }

    protected Corpus createSingleSourceCorpus(Source source) {
        return new SingleSourceCorpus(source);
    }

    protected Source getBrowserSource(Sources sources) {
        String name = getContext().getString(R.string.browser_search_component);
        return sources.getSource(name);
    }

    protected Source getAppsSource(Sources sources) {
        String name = getContext().getString(R.string.installed_apps_component);
        return sources.getSource(name);
    }

}
