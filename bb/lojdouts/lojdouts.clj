(ns lojdouts.lojdouts
  (:require [babashka.fs :as fs]
            [aero.core :refer [read-config]]
            [babashka.process :refer [sh]]))

(defn read-env
  ([env-name default] (or (System/getenv env-name) default))
  ([env-name] (System/getenv env-name)))

(def config-home
  (read-env "XDG_CONFIG_HOME" (fs/expand-home "~/.config")))

(def loadout-file (str config-home "/lojdouts/loadouts.edn"))

(def loadouts (read-config loadout-file))

(defn add-dep [dep]
  (sh (format "neil dep add %s" dep)))

(defn add-deps [deps]
  (doall (map add-dep deps)))

(defn add-deps-from-loadout [loadout-name]
  (let [loadout ((keyword loadout-name) loadouts)]
    (add-deps loadout)))

(defn -main
  [& args]
  (add-deps-from-loadout (first args)))
  
;; TODO: 
;; Let us chose to add to bb.edn instead of deps.edn
;; Maybe add to pods
;; Maybe specify the profiles to add to in the config
