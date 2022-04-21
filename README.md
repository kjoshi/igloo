# Igloo
A ClojureScript app to help you keep track of what's in your freezer.

We do a lot a [batch cooking](https://www.safefood.net/how-to/batch-cooking) and were struggling to keep track of what meals were in the freezer and how long they'd been there for.
This app is our solution to that.

## [Use Igloo here](https://igloo.gegin.dev)

Igloo lets you add meals to your virtual freezer and keep track of when they were put in there, how many are left and roughly where in the freezer they are.

The meals data is stored only in your browser's [localStorage](https://blog.logrocket.com/localstorage-javascript-complete-guide/) - clearing your browsing history would mean the data would be lost.

# Code structure
Igloo was mainly an exercise in learning [ClojureScript](https://clojurescript.org/). 

It has been created using the [re-frame](https://github.com/day8/re-frame) framework, which uses [Reagent](https://github.com/reagent-project/reagent) to create React components. 
Styling is done using [TailwindCSS](https://tailwindcss.com/) (the project was initially created using [this template](https://github.com/jacekschae/shadow-cljs-tailwindcss)), but plans are to replace the direct tailwind usage with something like [Ornament](https://github.com/lambdaisland/ornament).

Other notable dependencies are [fork](https://github.com/luciodale/fork) for creating forms, [vlad](https://github.com/logaan/vlad) for form validation and [HeadlessUI](https://headlessui.dev/) for modal and drop-down menu components.

The app is not structured exactly as suggested in the [re-frame documentation](https://github.com/day8/re-frame/blob/master/docs/App-Structure.md), it's a bit of a hybrid of the [smaller app](https://github.com/day8/re-frame/blob/master/docs/App-Structure.md#a-smaller-app) and [larger app](https://github.com/day8/re-frame/blob/master/docs/App-Structure.md#larger-apps) layouts.
<pre>
src/main/igloo
├── core.cljs			<--- main entry point
├── components.cljs		<--- Reagent components used in several places in the app
├── db.cljs			<--- schema, validation, etc  (data layer)
├── features			<--- Each feature is a (more-or-less) standalone set of view components & re-frame events/subscriptions
│   ├── common.cljs		  <--- re-frame stuff common to several features
│   ├── announcement.cljs	  <--- The 'Welcome' splash screen
│   ├── configuration.cljs	  <--- The 'Setup'/'Configuration' form
│   ├── item.cljs		  <--- Each item in the meal list
│   └── item_form.cljs		  <--- The add/edit item form
├── init.cljs			<--- Re-frame stuff to initialise the `app-db`
├── util.cljs			<--- Shared utility functions
└── views.cljs			<--- The main views, which stitch together the features
<pre>

