(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert! filled?]]
            [cljss.utils :refer [build-css]]
            [clojure.string :as cstr]))

(def ^:private sheets (atom (list (create-sheet))))

(defn css
  "Takes class name, static styles and dynamic styles.
   Injects styles and returns a string of generated class names."
  [cls static vars]
  (let [sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css cls static vars))
      (do
        (when-not (empty? static)
          (insert! sheet static cls))
        (if (pos? (count vars))
          (let [var-cls (str "vars-" (hash vars))]
            (insert! sheet #(build-css var-cls vars) var-cls)
            (str cls " " var-cls))
          cls)))))

(defn css-keyframes
  "Takes CSS animation name, static styles and dynamic styles.
   Injects styles and returns generated CSS animation name."
  [cls static vars]
  (let [sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css-keyframes cls static vars))
      (let [inner
            (reduce
              (fn [s [id val]] (cstr/replace s id val))
              static
              vars)
            anim-name (str "animation-" cls "-" (hash vars))
            keyframes (str "@keyframes " anim-name "{" inner "}")]
        (insert! sheet keyframes anim-name)
        anim-name))))
