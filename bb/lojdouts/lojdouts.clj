(ns lojdouts.lojdouts
  (:require [clojure.string :as str]
            [babashka.fs :as fs]
            [clojure.edn :as edn]
            [borkdude.rewrite-edn :as r]
            [aero.core :as aero]
            [babashka.cli :as cli])
  (:gen-class))

(def windows? (fs/windows?))

(def bb? (System/getProperty "babashka.version"))

(def deps-template (str/triml "
{:deps {}
 :aliases {}}
"))

(def bb-template (str/triml "
{:deps {}
 :tasks
 {
 }}
"))

(defn get-env
  ([var-name default] (or (System/getenv var-name) default))
  ([var-name] (get-env var-name nil)))

(def config-dir (or (get-env "XDG_CONFIG_HOME") (fs/expand-home "~/.config")))

(def loadout-file (str config-dir "/lojdouts/loadouts.edn"))
(def loadouts (aero/read-config loadout-file))

(defn ensure-deps-file
  [opts]
  (let [target (:deps-file opts)]
    (when-not (fs/exists? target)
      (spit target (if (= "bb.edn" target) bb-template deps-template)))))

(defn edn-string [opts] (slurp (:deps-file opts)))

(defn edn-nodes [edn-string] (r/parse-string edn-string))

(defn merge*
  "Merge values from map `m` into node `node`."
  [node m]
  (loop [main-node node
         m-kvs     (vec m)]
    (if (empty? m-kvs)
      main-node
      (let [[k v] (first m-kvs)]
        (recur (r/assoc main-node k v) (rest m-kvs))))))

(defn deep-merge*
  "Recursively merge values from map `b` into node `a`."
  [a b]
  (if (map? (r/sexpr a))
    (merge* a (for [[k v] b] [k (deep-merge* (r/get a k) v)]))
    b))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(def command-table
  [{:cmds [] :fn (partial merge-loadout "deps.edn" ) :args->opts [:loadout]}])

(def spec
  {:lofile    {:desc "Use a specific loadout-file", :coerce :string, :alias :f},
   :bb        {:desc   "Act on bb.edn instead of deps.edn",
               :coerce :boolean,
               :alias  :b},
   :deps-file {:desc   "Path to deps/bb.edn file to operate on",
               :coerce :string,
               :alias  :d},
   :loedit    {:desc   "Open up your loadout.edn file in $EDITOR",
               :coerce :boolean,
               :alias  :e}})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (cli/dispatch command-table
                *command-line-args*
                {:spec spec, :exec-args {:deps-file "deps.edn"}})
  nil)

(when (= *file* (System/getProperty "babashka.file")) (-main))
