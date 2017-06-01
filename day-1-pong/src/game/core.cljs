(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def LEFT 37)
(def UP 38)
(def RIGHT 39)
(def DOWN 40)
(def A 65)
(def D 68)

(def pw 80)
(def bs 5)

(def init-state
  {:p1x 160, :p2x 160,
   :p1l false, :p2l false,
   :p1r false, :p2r false,
   :bx 190, :by 190,
   :xv bs, :yv bs})

(defonce game-state (atom init-state))

(defn set-v! [k v]
  (swap! game-state assoc k v))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (set-v! :p1l true)
      (= RIGHT kc) (set-v! :p1r true)
      (= A kc) (set-v! :p2l true)
      (= D kc) (set-v! :p2r true))))

(defn on-key-up [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (set-v! :p1l false)
      (= RIGHT kc) (set-v! :p1r false)
      (= A kc) (set-v! :p2l false)
      (= D kc) (set-v! :p2r false))))

(.addEventListener js/document "keydown" on-key-down)
(.addEventListener js/document "keyup" on-key-up)

(defn bp-coll [bx px]
  (and (> (+ bx 20) px)
       (< bx (+ px pw))))

(defn update-state [{:keys [p1l p1r p2l p2r p1x p2x bx by xv yv] :as state}]
  (let [p1v (+ (if p1l -10 0) (if p1r 10 0))
        p2v (+ (if p2l -10 0) (if p2r 10 0))]
    (-> state
        (update :p1x + p1v)
        (update :p2x + p2v)
        (update :bx + xv)
        (update :by + yv)
        (cond-> (< bx 0) (assoc :xv bs)
                (> bx 380) (assoc :xv (- bs))
                (and (< by 20) (bp-coll bx p2x)) (assoc :yv bs)
                (and (> by 360) (bp-coll bx p1x)) (assoc :yv (- bs))
                (or (< by -20) (> by 400)) (assoc
                                            :bx (:bx init-state)
                                            :by (:by init-state))))))

(defn draw-state [{:keys [p1x p2x bx by]}]
  (set! (.-fillStyle ctx) "black")
  (.fillRect ctx 0 0 400 400)

  (set! (.-fillStyle ctx) "grey")
  (.fillRect ctx p1x 380 pw 20)
  (.fillRect ctx p2x 0 pw 20)

  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx bx by 20 20))

(defn game-loop []
  (swap! game-state update-state)
  (draw-state @game-state))

(defonce interval-id (js/setInterval game-loop (/ 1000 60)))
