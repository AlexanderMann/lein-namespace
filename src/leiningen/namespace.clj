(ns leiningen.namespace
  (:require [leiningen.core.eval :refer [eval-in-project]]
            [clojure.tools.namespace.find :as ns.find]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (java.nio.file Paths)))

(defn get-require-as
  "Return the :as declaration in a requirement:
  [clojure.java.io :as io] -> 'io"
  [requirement]
  )

(defn get-require-refer
  [])

(defn get-requires
  "Filter an ns-decl's references for the :require s-expression."
  [references]
  (->> references
       (filter (fn [[reference-type]] (= :require reference-type)))
       first))

(defn get-requireds
  [ns-symbol [_:require & requirements]]
  (reduce (fn [accum [ns-path & ns-args]]
            (if (get-in accum [ns-path ns-args])
              (update-in accum [ns-path ns-args] conj ns-symbol)
              (assoc-in accum [ns-path ns-args] #{ns-symbol})))
          {}
          requirements))

(defn build-requireds-map
  [[_ns ns-symbol & references]]
  (get-requireds ns-symbol
                 (get-requires references)))

(defn consolidated-requires
  "Given a project's unevaluated namespace declarations, build a full requires map:
  {'ns-path {'ns-args ['ns-symbol-referencing '...]}}

  ex:
  {clojure.spec.alpha {(:as s)       #{my-project.core}}
   clojure.future     {(:refer :all) #{my-project.core my-project.helper}
                       (:as future}  #{my-project.crimes my-project.sins}
   ...}"
  [ns-decls]
  (reduce (fn [accum x]
            (merge-with (partial merge-with into)
                        accum
                        x))
          {}
          (map build-requireds-map
               ns-decls)))

(defn rule--all-ns-alphabetized
  [ns-decls])

(defn rule--all-refers-alphabetized
  [ns-decls])

(defn rule--all-imports-alphabetized
  [ns-decls])

(defn rule--all-alphabetized
  [ns-decls])

(defn rule--no-differing-names
  [ns-decls]
  (not-empty
    (filter (fn [[_ v]]
              (not= 1 (count v)))
            (consolidated-requires ns-decls))))

(defn rule--clojure-future-is-always-all
  [ns-decls]
  (let [future-references (get (consolidated-requires ns-decls)
                               'clojure.future)]
    (def snag future-references)
    (when (and (not-empty future-references)
               (not= '((:refer :all)) (keys future-references)))
      future-references)))

(def snag (consolidated-requires
               (ns.find/find-ns-decls [(io/file "/Users/alexandermann/git/lein-namespace/")])))

(rule--no-differing-names
  (ns.find/find-ns-decls [(io/file "/Users/alexandermann/git/lein-namespace/")]))

(comment

  (get-requireds
    (remove #{:require}
            (apply concat (map get-requires
                               (ns.find/find-ns-decls [(io/file "/Users/alexandermann/git/lein-namespace/")])))))

  (file-seq (io/file "/Users/alexandermann/git/lein-namespace/"))

  (slurp (io/file "/Users/alexandermann/git/lein-namespace/README.md"))
  )

(defn namespace
  "I don't do a lot."
  [project & args]
  (println "Hi!"))
