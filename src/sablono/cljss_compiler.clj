(ns sablono.cljss-compiler
  (:require [sablono.compiler :as s]
            [sablono.util :as sutil]
            [cljss.core :as cljss]))

(defn- compile-class [class styles]
  (let [gen-class `(cljss.core/css ~@(cljss/build-styles styles))]
    (if (seq class)
      `(apply str ~gen-class " " ~@(interpose " " class))
      gen-class)))

;; WARNING: NEVER EVER DO THIS AT HOME!
;; swap `sablono.compiler/compile-attrs` fn with own, adjusted, implementation
(alter-var-root
  #'s/compile-attrs
  (fn [_]
    (fn [{:keys [class className class-name] :as attrs}]
      (let [class (filter identity [class className class-name])]
        (->> (seq attrs)
             (reduce (fn [attrs [name value]]
                       (if (= name :css)
                         (-> attrs
                             (assoc :class (compile-class class value))
                             (dissoc :css))
                         (assoc attrs name (s/compile-attr name value))))
                     nil)
             (sutil/html-to-dom-attrs)
             (s/to-js))))))