(require '[babashka.deps :as deps])
(deps/add-deps '{:deps {org.clojure/clojure  {:mvn/version "1.11.1"},
                        aero/aero            {:mvn/version "1.1.6"},
                        borkdude/rewrite-edn {:mvn/version "0.4.6"},
                        org.babashka/cli     {:mvn/version "0.6.49"}}})

(ns lojdouts.lojdouts
  (:require [babashka.fs :as fs]
            [borkdude.rewrite-edn :as r]
            [aero.core :as aero]
            [babashka.cli :as cli]
            [clojure.pprint :refer [pprint]]
            [babashka.process :refer [shell]])
  (:gen-class))

; (def windows? (fs/windows?))
;
; (def bb? (System/getProperty "babashka.version"))
;
; (def deps-template (str/triml "
; {:deps {}
;  :aliases {}}
; "))
;
; (def bb-template (str/triml "
; {:deps {}
;  :tasks
;  {
;  }}
; "))

(defn get-env
  ([var-name default] (or (System/getenv var-name) default))
  ([var-name] (get-env var-name nil)))

(def config-dir (or (get-env "XDG_CONFIG_HOME") (fs/expand-home "~/.config")))

(def loadout-file (str config-dir "/lojdouts/loadouts.edn"))
(def loadouts (aero/read-config loadout-file))

; (defn ensure-deps-file
;   [opts]
;   (let [target (:deps-file opts)]
;     (when-not (fs/exists? target)
;       (spit target (if (= "bb.edn" target) bb-template deps-template)))))

;(defn edn-string [opts] (slurp (:deps-file opts)))

(defn edn-nodes [edn-string] (r/parse-string edn-string))

(defn nodes-of
  [edn-file]
  (-> edn-file
      slurp
      edn-nodes))

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

(defn merge-loadout
  "Merge contents of the named loadout into the target deps-file (usually 
  deps.edn or bb.edn).
  
  Returns the new nodes."
  [deps-file loname]
  (let [lo         (get loadouts loname)
        deps-nodes (nodes-of deps-file)]
    ;(print {:lo lo :nodes deps-nodes})
    (deep-merge* deps-nodes lo)))

(defn add-loadout!
  [{:keys [opts]}]
  ;(pprint opts)
  (let [deps-file (if (:bb opts) (str (fs/cwd) "/bb.edn") (:deps-file opts))]
    (spit deps-file
          (->> opts
               :loadout
               keyword
               (merge-loadout deps-file)))))

(defn view-loadout!
  [{:keys [opts]}]
  (pprint (get loadouts (keyword (:loadout opts)))))

(defn list-loadouts! [_opts] (pprint (keys loadouts)))

(defn edit-loadouts!
  [_opts]
  (let [editor-cmd (get-env "EDITOR" (get-env "VISUAL" "vi"))]
    (shell (format "%s %s" editor-cmd loadout-file))))

(comment
  (add-loadout! {:bb true, :loadout "mysql"})
  (merge-loadout (str (fs/cwd) "/deps.edn") :mysql)
  (add-tap (bound-fn* pprint)))

(def command-table
  [{:cmds ["add"], :fn add-loadout!, :args->opts [:loadout]}
   {:cmds ["view"], :fn view-loadout!, :args->opts [:loadout]}
   {:cmds ["list"], :fn list-loadouts!} {:cmds ["edit"], :fn edit-loadouts!}])

(def spec
  {:lofile    {:desc    "Use a specific loadout-file",
               :coerce  :string,
               :alias   :f,
               :default loadout-file},
   :bb        {:desc    "Act on bb.edn by default instead of deps.edn",
               :coerce  :boolean,
               :alias   :b,
               :default false},
   :deps-file {:desc    "Path to deps/bb.edn file to operate on",
               :coerce  :string,
               :alias   :d,
               :default "deps.edn"},
   :loedit    {:desc    "Open up your loadout.edn file in $EDITOR",
               :coerce  :boolean,
               :alias   :e,
               :default false},
   :loadout   {:desc "The name of the loadout to use", :coerce :string},
   :list      {:desc    "List accessible loadouts",
               :coerce  :boolean,
               :alias   :l,
               :default false},
   :help      {:desc    "Print help",
               :coerce  :boolean,
               :alias   :h,
               :default false}})

;; Recommending adding a `lojd` alias set as `lojb`, which is set to `lojd
;; --bb`
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (cli/dispatch command-table
                args
                {:spec      spec,
                 :exec-args {:deps-file (str (fs/cwd) "/deps.edn")}})
  nil)

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
