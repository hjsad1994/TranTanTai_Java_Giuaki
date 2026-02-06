# Inventory Management - Issues

## Problems & Gotchas

## 2026-02-05: Session 2 - Initial Assessment

### Observations
- Admin forms (add.html, edit.html) do NOT have quantity input field yet
- Admin list.html does NOT have quantity column
- book/detail.html does NOT show stock status or disable button
- book/list.html does NOT show out-of-stock indicators
- CartService.saveCart() does NOT validate stock before checkout

### Missing Items to Implement
1. Quantity input in admin forms
2. Quantity column + badges in admin list
3. Stock status display on public book pages
4. Stock validation in checkout flow
