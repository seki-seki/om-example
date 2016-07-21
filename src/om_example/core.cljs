(ns ^:figwheel-always om-example.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [clojure.string :as string]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [put! <! chan]]))

(enable-console-print!)

(defonce app-state (atom {:name ["Lion" "Zebra" "Buffalo" "Antelope"]}))



(defcomponent list-view [data owner]
  (init-state [_]
              {:channel (chan)})
  (will-mount [_]
              (let [channel (om/get-state owner :channel)]
                (go-loop []
                         (let [[cmd name] (<! channel)]
                           (try
                             (case cmd
                               :delete (om/transact! data :name
                                                     (fn [names]
                                                       (remove #(= name %) names))))
                             (catch js/Error e))
                           (when (not= cmd :close)
                             (recur))))))
  (render-state [_ {:keys [channel]}]
          (html [:div
                 [:h2 "List"]
                 [:ul
                  (for [n (:name data)]
                    [:li n
                    [:i.red.delete.icon
                 {:on-click (fn [_] (put! channel [:delete n]))}]])]]))
  (will-unmount [_]
                (let [channel (om/get-state owner :channel)]
                  (put! channel [:close true]))))

(om/root list-view app-state
         {:target (. js/document (getElementById "app"))})
