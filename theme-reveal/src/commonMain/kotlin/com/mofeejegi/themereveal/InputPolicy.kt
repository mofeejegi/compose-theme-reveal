package com.mofeejegi.themereveal

/**
 * What happens to pointer input inside the host while a reveal runs.
 *
 * The input law for blocked reveals: never cancel, always accelerate — a tap
 * fast-forwards the reveal to completion, it never rewinds or aborts. The
 * reveal always ends at the same place; only its duration varies.
 */
sealed interface InputPolicy {

    /**
     * Consume all pointer input inside the host until full coverage; a tap
     * accelerates the reveal to completion. The register for ceremonies.
     */
    data object Block : InputPolicy

    /**
     * Leave input alone. For hosts whose content is static or whose
     * interaction state lives outside the dual-composed tree (e.g. a pager
     * page revealing after settle — the drag gesture belongs to the pager,
     * not the page). Content-internal scrollables are the caller's own risk:
     * interaction state remembered inside the content lambda is duplicated
     * across the two compositions and will desync.
     */
    data object Allow : InputPolicy
}
