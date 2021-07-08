package asaa.bachelor.bleconnector.connections.connection.multi

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import asaa.bachelor.bleconnector.connections.connection.multi.commands.*

class CommandsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    val fragments = listOf(
        ConnectFragment(),
        ReadFragment(),
        ConnectionSettingsFragment(),
        NotifyIndicateFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}