package com.piccollage.jcham.jaimelaboratory

inline fun <T, R> T.iff(block: (T) -> R): R? {
  if (this == null || this == false) return null
  return block(this)
}
